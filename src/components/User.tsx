import type { User } from "@/types";
import { Button } from "./Button";
import { ChatIcon } from "@/icons/ChatIcon";
import { useAppDispatch } from "@/redux/store";
import { authLogout } from "@/features/auth/authActions";
import { useNavigate } from "react-router";

export interface UserViewProps {
  user: User;
  type: "personal" | "public";
}

function UserView({ user, type }: UserViewProps) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const onLogoutHandler = () => {
    localStorage.removeItem("token");
    dispatch(authLogout());
    navigate("/");
  };

  return (
    <>
      <section className="container mx-auto py-16 flex flex-col-reverse items-center gap-16 sm:flex-row sm:gap-4">
        <div className="w-full">
          <h1 className="text-secondary text-4xl mb-8">
            Аккаунт пользователя <br /> {`@${user?.username}`}
          </h1>
          <p className="text-secondary">Дата регистрации {user?.dateOfReg}</p>
          {type == "personal" && (
            <p className="text-secondary">Роль: {user?.role}</p>
          )}
        </div>
        <img
          src={"https://placehold.co/600x400"}
          alt="User profile photo"
          className="aspect-square object-cover w-auto h-auto sm:flex-1/4 sm:w-full"
        />
      </section>
      {type == "personal" ? (
        <section className="container mx-auto flex flex-col gap-8 items-start">
          <h1 className="text-primary text-4xl">
            Баланс: {`${user?.balance} Р`}
          </h1>
          <div className="flex gap-8">
            <Button>Вывести</Button>
            <Button>Пополнить</Button>
          </div>
          <Button onClick={onLogoutHandler} variant="destructive">
            Выйти из аккаунта
          </Button>
        </section>
      ) : (
        <section className="container mx-auto">
          <Button variant="secondary" size="lg">
            <ChatIcon className="mr-2" /> Написать продавцу
          </Button>
        </section>
      )}
      {/* {type == "personal" && (
        <section className="container mx-auto">
          <h1 className="text-secondary text-4xl mb-8">Роль: {user?.role}</h1>
        </section>
      )} */}
      {/* TODO: User reviews */}
    </>
  );
}

export { UserView };
