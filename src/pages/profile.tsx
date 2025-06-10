import { Button } from "@/components/Button";
import { UserView } from "@/components/UserView";
import { authLogout } from "@/features/auth/authActions";
import { useUser } from "@/hooks/useUser";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useNavigate } from "react-router";

export default function ProfilePage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { user: userInfo } = useAppSelector((state) => state.auth);
  const { user, error, isLoading } = useUser(userInfo?.id || "");

  useEffect(() => {
    if (!userInfo) {
      navigate("/login");
    }
  }, []);

  const onLogoutHandler = () => {
    localStorage.removeItem("token");
    dispatch(authLogout());
    navigate("/");
  };

  return (
    <>
      {user && <UserView type="personal" user={{ ...userInfo, ...user }} />}
      {error && (
        <section className="container mx-auto mb-16 flex items-center justify-center py-16">
          <Button onClick={onLogoutHandler} variant="destructive">
            Выйти из аккаунта
          </Button>
        </section>
      )}
      {isLoading && <h1>Загрузка...</h1>}
    </>
  );
}
