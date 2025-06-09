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
import { AddProductForm } from "@/components/AddProductForm";
import game from "@/assets/cs2.webp";
import { ProductView } from "./ProductView";

export interface UserViewProps {
  user: User;
  type: "personal" | "public";
}

const products = [
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 1000,
    favourite: false,
    id: 1
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 2000,
    favourite: true,
    id: 2
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 3000,
    favourite: false,
    id: 3
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 4000,
    favourite: false,
    id: 4
  }
];

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

      <section className="container mx-auto mb-16 flex flex-col gap-8">
        <h1 className="text-secondary text-4xl">Мои объявления:</h1>

        <Dialog>
          <DialogTrigger asChild>
            <Button variant="secondary" className="self-start">
              Добавить объявление
            </Button>
          </DialogTrigger>
          <DialogContent className="gap-8 w-full sm:max-w-2xl">
            <DialogHeader>
              <DialogTitle>Создать объявление</DialogTitle>
            </DialogHeader>
            <AddProductForm></AddProductForm>
          </DialogContent>
        </Dialog>

        {/* TODO: Load users ads */}
        {products.map((product) => (
          <ProductView show_favourite={false} product={product} />
        ))}
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
      {/* TODO: User reviews */}
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
    </>
  );
}

export { UserView };
