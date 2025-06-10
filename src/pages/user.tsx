import { UserView } from "@/components/UserView";
import { resetUser } from "@/features/user/userActions";
import { fetchUser } from "@/features/user/userReducer";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useParams } from "react-router";

export default function UserPage() {
  let { userId } = useParams<{ userId?: string }>();
  const dispatch = useAppDispatch();
  const { data, status } = useAppSelector((state) => state.users);

  useEffect(() => {
    if (userId != null) {
      // TODO: Abort previous request before new one
      dispatch(fetchUser(userId));

      // Cleans up user data when unmounting
      return () => {
        dispatch(resetUser());
      };
    }
  }, [dispatch, userId]);

  // TODO: Loading spinner
  return (
    <>
      {data && <UserView type="public" user={data} />}
      {status == "loading" && <h1>Загрузка...</h1>}
    </>
  );
}
