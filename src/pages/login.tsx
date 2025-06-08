import { LoginButton } from "@/components/LoginButton";
import { login } from "@/features/auth/authReducer";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import type { TelegramAuthData } from "@/types";
import { useEffect } from "react";
import { useNavigate } from "react-router";

export default function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { token, user, status } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (token && status == "succeeded") {
      navigate("/profile");
    }
  }, [token]);

  const onAuthHandler = (user: TelegramAuthData) => {
    console.log(user);
    dispatch(login(user));
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
