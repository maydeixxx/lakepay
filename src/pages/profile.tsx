import { UserView } from "@/components/UserView";
import { useUser } from "@/hooks/useUser";
import { useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useNavigate } from "react-router";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user: userInfo } = useAppSelector((state) => state.auth);
  const { user, error, isLoading } = useUser(userInfo?.id || "");

  useEffect(() => {
    if (!userInfo) {
      navigate("/login");
    }
  }, []);

  return (
    <>
      {user && <UserView type="personal" user={{ ...userInfo, ...user }} />}
      {isLoading && <h1>Загрузка...</h1>}
    </>
  );
}
