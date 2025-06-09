import { UserView } from "@/components/User";
import { resetUser } from "@/features/user/userActions";
import { fetchUser } from "@/features/user/userReducer";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useNavigate } from "react-router";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);

  const dispatch = useAppDispatch();
  const { data, status } = useAppSelector((state) => state.users);

  useEffect(() => {
    if (!user) {
      navigate("/login");
    } else {
      // Load user balance
      dispatch(fetchUser(user.id.toString()));

      // Cleans up user data when unmounting
      return () => {
        dispatch(resetUser());
      };
    }
  }, []);
  // TODO: Loading spinner
  return (
    <>
      {data && <UserView type="personal" user={{ ...user, ...data }} />}
      {status == "loading" && <h1>Загрузка...</h1>}
    </>
  );
}
