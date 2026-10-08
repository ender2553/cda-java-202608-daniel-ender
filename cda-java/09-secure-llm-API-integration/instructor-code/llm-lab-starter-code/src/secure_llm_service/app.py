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

from fastapi import FastAPI
from pydantic import BaseModel

from .config import Config, load_config


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

    # TODO(Lesson 8): implement POST /chat using get_cfg(), build_messages(),
    #   complete(), and filter_output(). Reject unsafe input with 400 + a reason
    #   code; convert upstream failures into a generic 502; never leak internals.

    return app
