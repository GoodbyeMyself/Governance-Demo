#!/usr/bin/env python3
# 本地 SMTP 捕获服务。
# 用于单容器演示环境接收认证中心发出的验证码邮件，并把原始邮件落盘，方便排查和查看验证码。

import asyncore
import os
import smtpd
import sys
import time
import uuid
from email import policy
from email.parser import BytesParser


MAIL_DIR = os.environ.get("MAIL_CATCHER_DIR", "/opt/governance-demo/mailbox")
MAIL_HOST = os.environ.get("MAIL_CATCHER_HOST", "0.0.0.0")
MAIL_PORT = int(os.environ.get("MAIL_CATCHER_PORT", "1025"))


class FileMailCatcher(smtpd.SMTPServer):
    """把收到的邮件保存为 .eml 文件。"""

    def process_message(self, peer, mailfrom, rcpttos, data, **kwargs):
        os.makedirs(MAIL_DIR, exist_ok=True)

        if isinstance(data, str):
            raw_data = data.encode("utf-8")
        else:
            raw_data = data

        filename = f"{time.strftime('%Y%m%d-%H%M%S')}-{uuid.uuid4().hex}.eml"
        file_path = os.path.join(MAIL_DIR, filename)
        with open(file_path, "wb") as mail_file:
            mail_file.write(raw_data)

        try:
            message = BytesParser(policy=policy.default).parsebytes(raw_data)
            subject = message.get("Subject", "")
        except Exception:
            subject = ""

        print(
            f"[MAIL] saved={file_path} from={mailfrom} to={','.join(rcpttos)} subject={subject}",
            flush=True,
        )
        return None


def main():
    os.makedirs(MAIL_DIR, exist_ok=True)
    FileMailCatcher((MAIL_HOST, MAIL_PORT), None)
    print(
        f"[MAIL] File mail catcher started at {MAIL_HOST}:{MAIL_PORT}, output dir: {MAIL_DIR}",
        flush=True,
    )
    try:
        asyncore.loop()
    except KeyboardInterrupt:
        print("[MAIL] Mail catcher stopped", flush=True)
        sys.exit(0)


if __name__ == "__main__":
    main()
