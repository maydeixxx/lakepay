import { Button } from "@/components/Button";
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
    }
  }, [dispatch, userId]);

  // TODO: Loading spinner
  return (
    <>
      <section className="container mx-auto py-16 flex flex-col-reverse items-center gap-16 sm:flex-row sm:gap-4">
        <div className="w-full">
          <h1 className="text-secondary text-4xl mb-8">
            Аккаунт пользователя <br /> {`@${data?.username}`}
          </h1>
          <p className="text-secondary">Дата регистрации {data?.dateOfReg}</p>
        </div>
        <img
          src={"https://placehold.co/600x400"}
          alt="User profile photo"
          className="aspect-square object-cover w-auto h-auto sm:flex-1/4 sm:w-full"
        />
      </section>
      <section className="container mx-auto pb-16">
        <h1 className="text-primary text-4xl mb-8">
          Баланс: {`${data?.balance} Р`}
        </h1>
        <div className="flex gap-8">
          <Button>Вывести</Button>
          <Button>Пополнить</Button>
        </div>
      </section>
      <section className="container mx-auto">
        <h1 className="text-secondary text-4xl mb-8">Роль: {data?.role}</h1>
      </section>
      {/* TODO: User reviews */}
    </>
  );
}
