import { UserView } from "@/components/User";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useNavigate } from "react-router";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { token, user, status } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (!user) {
      navigate("/login");
    }
  }, []);
  // TODO: Loading spinner
  return (
    <>
      {user && <UserView type="personal" user={user} />}
      {status == "loading" && <h1>Загрузка...</h1>}
    </>
  );
}
