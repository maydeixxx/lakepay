import { Button } from "@/components/Button";
import { NavLink } from "react-router";

export default function NotFoundPage() {
  return (
    <>
      <div className="container mx-auto py-16 text-center">
        <h1 className="text-secondary text-4xl mb-8">Страница не найдена!</h1>
        <p className="text-secondary text-2xl mb-16">
          К сожалению такой страницы не существует!
        </p>
        <NavLink to="/">
          <Button>На главную</Button>
        </NavLink>
      </div>
    </>
  );
}
