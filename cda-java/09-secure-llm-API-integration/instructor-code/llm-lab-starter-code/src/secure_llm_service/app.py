"""FastAPI request-handling layer.

STARTER — Lesson 8 (Capstone assembly).

/health works. Your job in the capstone is to implement POST /chat by wiring the
controls in order:
    sanitize input (prompt_builder)
      -> call the model through the key-holding client (llm_client)
      -> filter output (output_filter)
      -> return {"reply": ..., "redactions": ...}
Fail closed: any internal error becomes a generic 4xx/5xx with no leaked detail.
"""
from __future__ import annotations

import logging

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from .config import Config, load_config
from .llm_client import LLMUnavailableError, complete
from .output_filter import filter_output
from .prompt_builder import UnsafeInputError, build_messages
from .response_handler import LLMParseError


logger = logging.getLogger("llm_service")


class ChatRequest(BaseModel):
    message: str


class ChatResponse(BaseModel):
    reply: str
    redactions: dict


def create_app(cfg: Config | None = None) -> FastAPI:
    app = FastAPI(title="Secure LLM Service (starter)")

    state: dict = {"cfg": cfg}

    def get_cfg() -> Config:
        if state["cfg"] is None:
            state["cfg"] = load_config()
        return state["cfg"]

    @app.get("/health")
    def health() -> dict:
        # Liveness only — must not call the LLM API.
        return {"status": "ok"}

    @app.post("/chat", response_model=ChatResponse)
    def chat(req: ChatRequest) -> ChatResponse:
        
        try:
            messages = build_messages(
                "You are a helpful assistant.",
                req.message,
                max_chars=get_cfg().max_input_chars,
            )
        except UnsafeInputError as e:
            logger.info(
                "input_rejected length=%d reason=%s",
                len(req.message),
                e.reason_code,
            )
            raise HTTPException(
                status_code=400,
                detail=e.reason_code,
            ) from None

        
        try:
            cfg = get_cfg()
            result = complete(messages, cfg)

            filtered = filter_output(
                result.content,
                max_output_length=cfg.max_output_length,
            )

        except (LLMUnavailableError, LLMParseError):
            raise HTTPException(
                status_code=502,
                detail="UPSTREAM_ERROR",
            ) from None

        except Exception:
            logger.error("chat_request_failed")
            raise HTTPException(
                status_code=500,
                detail="INTERNAL_ERROR",
            ) from None

        
        logger.info("output_redactions counts=%s", filtered.counts)

        # Step 4: Return only the filtered response.
        return ChatResponse(
            reply=filtered.text,
            redactions=filtered.counts,
        )

    return app