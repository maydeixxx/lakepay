import { fetchUser } from "@/features/user/userReducer";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useParams } from "react-router";

export default function UserPage() {
  let { userId } = useParams<{ userId?: string }>();
  const dispatch = useAppDispatch();
  const user = useAppSelector((state) => state.users.data);

  useEffect(() => {
    if (userId != null) {
      dispatch(fetchUser(userId));
    }
  }, [dispatch, userId]);

  console.log(user);

  return <h1>hello</h1>;
}
