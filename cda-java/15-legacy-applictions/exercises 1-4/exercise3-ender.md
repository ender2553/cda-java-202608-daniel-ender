Card 4 — Customer Email Address

Group Dissection Sheet

Card #: 4 — Customer Email Address

| Part | Our answer |
|---|---|
| **Hidden dependency** (point to the line) | `db.customers[customer_id].email = new_email` — the legacy function assumes the caller has already authenticated the customer, verified their password, and confirmed ownership of the new email address. |
| **Trigger** (which of the four, and what exactly changed) | **New privilege / new data path.** The Marketing Preferences service allows unauthenticated users to access a preferences page using a customer ID in the URL and directly change the customer's email address without the original authentication and confirmation process. |
| **Resulting vulnerability** (the named, exploitable condition) | **Broken access control / account takeover.** An attacker could change another customer's email address and then use the Forgot Password feature to request a reset link sent to the attacker's email, potentially taking over the victim's account. |
| **Trap class** | **Privilege escalation + implicit contract + side effect.** The function relies on security checks performed by its original caller, and changing the email has security consequences for the password-reset system. |
| **Discovery step** (the one check that would have caught it) | Perform an **authorization review and abuse-case test** of the email-change flow, verifying that an unauthenticated user cannot change another customer's email or bypass email ownership confirmation. |

Did you find a second hidden dependency on your card? If so, what is it?
The Forgot Password function trusts the email stored in the customer database. If another service can change that email without proper verification, the password-reset flow becomes vulnerable because it sends account recovery links to the newly stored address without knowing whether the address belongs to the real customer.

Attack chain (why this is dangerous)
1. Attacker accesses the public Marketing Preferences page.
2. Attacker changes the email address associated with a victim's customer ID.
3. The database now associates the victim's account with the attacker's email.
4. Attacker requests a password reset.
5. The reset link is sent to the attacker's email.
6. Attacker may gain control of the victim's account.

Key takeaway: The legacy function was safe only because its original caller enforced authentication and email verification. Reusing the function in a new service without reproducing those security controls turns an assumed trust relationship into a vulnerability.
