import { UserView } from "@/components/UserView";
import { resetUser } from "@/features/user/userActions";
import { fetchUser } from "@/features/user/userReducer";
import { useUser } from "@/hooks/useUser";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useParams } from "react-router";

export default function UserPage() {
  let { userId } = useParams<{ userId?: string }>();
  const dispatch = useAppDispatch();
  const { user, error, isLoading } = useUser(userId || "");

  return (
    <>
      {user && <UserView type="public" user={user} />}
      {isLoading && <h1>Загрузка...</h1>}
    </>
  );
}
