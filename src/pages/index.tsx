// TODO: Fetch popular categories from server
import game from "@/assets/cs2.webp";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { FavouriteIcon } from "@/icons/FavouriteIcon";
import { NavLink } from "react-router";
import { categories } from "@/config";

// TODO: Fetch new ads from server
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

export default function Home() {
  return (
    <>
      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Популярные категории</h1>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
          {categories.map((category) => (
            <Card className="bg-background py-0 overflow-hidden">
              <NavLink to={`/category/${category.name}`}>
                <img src={category.photo} alt={`Photo of ${category.title}`} />
                <h4 className="text-center py-2 font-bold">{category.title}</h4>
              </NavLink>
            </Card>
          ))}
        </div>
      </section>

      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Новые объявления</h1>
        <div className="flex flex-col gap-8">
          {products.map((product) => (
            <Card className="p-4 md:flex-row md:items-center justify-start">
              <NavLink to={`/product/${product.id}`}>
                <img
                  src={product.photo}
                  alt={product.title}
                  className="h-auto w-auto md:max-h-32 rounded-xl"
                />
              </NavLink>
              <div className="text-wrap grow">
                <h4 className="mb-4 font-bold">{product.title}</h4>
                <p className="text-xs md:text-sm">{product.description}</p>
              </div>
              <div className="flex gap-4 items-center">
                <Button variant="secondary">{`${product.price} Р`}</Button>
                {/* Add on click listener add/remove from favourites */}
                <Button size="icon" variant="secondary" className="p-2">
                  <FavouriteIcon
                    className={`${product.favourite ? "text-destructive" : "text-secondary-foreground"}`}
                  />
                </Button>
              </div>
            </Card>
          ))}
          <Card className="p-4 gap-0 min-h-32 justify-center">
            <h4 className="mb-4 font-bold">
              Хотите посмотреть больше аккаунтов?
            </h4>
            <p className="text-xs md:text-sm">
              Выберите нужную категорию, либо воспользуйтесь поиском!
            </p>
          </Card>
        </div>
      </section>
    </>
  );
}
