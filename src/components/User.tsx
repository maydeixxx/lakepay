import type { User } from "@/types";
import { Button } from "@/components/Button";
import { ChatIcon } from "@/icons/ChatIcon";
import { useAppDispatch } from "@/redux/store";
import { authLogout } from "@/features/auth/authActions";
import { useNavigate } from "react-router";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@/components/Dialog";
import { ProductForm } from "@/components/ProductForm";

export interface UserViewProps {
  user: User;
  type: "personal" | "public";
}

function UserView({ user, type }: UserViewProps) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  // TODO: Confirmation dialog before logout
  const onLogoutHandler = () => {
    localStorage.removeItem("token");
    dispatch(authLogout());
    navigate("/");
  };

  const personalProfile = () => (
    <>
      <section className="container mx-auto flex flex-col gap-8 items-start mb-16">
        <h1 className="text-primary text-4xl">
          Баланс: {`${user?.balance} Р`}
        </h1>
        <div className="flex gap-8">
          <Button>Вывести</Button>
          <Button>Пополнить</Button>
        </div>
      </section>

      <section className="container mx-auto mb-16">
        <h1 className="text-secondary text-4xl mb-8">Мои объявления:</h1>

        <Dialog>
          <DialogTrigger asChild>
            <Button variant="secondary">Добавить объявление</Button>
          </DialogTrigger>
          <DialogContent className="gap-8">
            <DialogHeader>
              <DialogTitle>Создать объявление</DialogTitle>
            </DialogHeader>
            <ProductForm></ProductForm>
          </DialogContent>
        </Dialog>
      </section>

      <section className="container mx-auto mb-16">
        <Button onClick={onLogoutHandler} variant="destructive">
          Выйти из аккаунта
        </Button>
      </section>
    </>
  );

  const publicProfile = () => (
    <>
      <section className="container mx-auto mb-8">
        <Button variant="secondary" size="lg">
          <ChatIcon className="mr-2" /> Написать продавцу
        </Button>
      </section>
    </>
  );

  return (
    <>
      <section className="container mx-auto my-16 flex flex-col-reverse items-center gap-16 sm:flex-row sm:gap-4">
        <div className="w-full">
          <h1 className="text-secondary text-4xl mb-8">
            Аккаунт пользователя <br /> {`@${user?.username}`}
          </h1>
          <p className="text-secondary">Дата регистрации {user?.dateOfReg}</p>
          {type == "personal" && (
            <p className="text-secondary">Роль: {user?.roles?.join(" ")}</p>
          )}
        </div>
        <img
          src={user.urlPhoto}
          alt="User profile photo"
          className="aspect-square object-cover w-auto h-auto sm:flex-1/4 sm:w-full"
        />
      </section>
      {type === "personal" ? personalProfile() : publicProfile()}
      {/* TODO: User reviews */}
    </>
  );
}

export { UserView };
