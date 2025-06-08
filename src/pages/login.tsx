import { LoginButton } from "@/components/LoginButton";
import type { TelegramAuthData } from "@/types";
import { useEffect, useRef } from "react";

export default function LoginPage() {
  const parent = useRef<HTMLDivElement | null>(null);

  const onAuthHandler = (user: TelegramAuthData) => {
    console.log(user);
  };

  return (
    <>
      <section className="container mx-auto py-16 flex flex-col items-center">
        <h1 className="text-secondary text-4xl mb-8">Войдите в аккаунт</h1>
        <LoginButton
          botUsername="mrayventgauth_bot"
          onAuthCallback={onAuthHandler}
          cornerRadius={10}
        />
      </section>
    </>
  );
}
