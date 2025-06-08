import { useEffect, useRef } from "react";
import type {
  LoginButtonProps,
  TTelegramAuthLogin,
  CreateScriptOptions
} from "@/types";

function initTelegramAuthLogin(options: TTelegramAuthLogin) {
  window.TelegramAuthLogin = options;
}

export function createScript({
  authCallbackUrl,
  botUsername,
  buttonSize = "large",
  cornerRadius,
  lang = "en",
  onAuthCallback,
  requestAccess = "write",
  showAvatar = true,
  widgetVersion = 22
}: CreateScriptOptions): HTMLScriptElement {
  const script = document.createElement("script");

  script.async = true;

  script.src = `https://telegram.org/js/telegram-widget.js?${widgetVersion}`;
  script.setAttribute("data-telegram-login", botUsername);
  script.setAttribute("data-size", buttonSize);
  if (cornerRadius) {
    script.setAttribute("data-radius", `${cornerRadius}`);
  }
  if (requestAccess) {
    script.setAttribute("data-request-access", requestAccess);
  }
  script.setAttribute("data-userpic", JSON.stringify(Boolean(showAvatar)));
  script.setAttribute("data-lang", lang);

  if (authCallbackUrl) {
    script.setAttribute("data-auth-url", authCallbackUrl);
  } else if (onAuthCallback) {
    script.setAttribute(
      "data-onauth",
      "TelegramAuthLogin.onAuthCallback(user)"
    );
  }

  return script;
}

export function LoginButton(props: LoginButtonProps) {
  const hiddenDivRef = useRef<HTMLDivElement>(null);
  const scriptRef = useRef<HTMLScriptElement>(null);

  useEffect(() => {
    // destroy the existing script element
    scriptRef.current?.remove();

    // init the global variable
    initTelegramAuthLogin({ onAuthCallback: props.onAuthCallback });

    // create a new script element and save it
    scriptRef.current = createScript(props);

    // add the script element to the DOM
    hiddenDivRef.current?.after(scriptRef.current);

    // Save siblings before unmount
    const siblings = hiddenDivRef.current?.parentElement?.children || [];

    return () => {
      // destroy the script element on unmount
      scriptRef.current?.remove();

      // We also need to remove the rendered iframe
      for (const element of siblings) {
        if (
          element instanceof HTMLIFrameElement &&
          element.src.includes("oauth.telegram.org")
        ) {
          element.remove();
          break;
        }
      }
    };
  }, [props]);

  return <div ref={hiddenDivRef} hidden />;
}
