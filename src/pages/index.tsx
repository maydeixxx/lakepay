import { Header } from "@/components/Header";
import favouriteIcon from "@/assets/favorite.svg";

// TODO: Fetch popular categories from server
import game from "@/assets/cs2-category.jpg";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
const categories = [
  {
    title: "CS2",
    url: "http://test1.com",
    photo: game
  },
  {
    title: "Fortnite",
    url: "http://test2.com",
    photo: game
  },
  {
    title: "Dota 2",
    url: "http://test3.com",
    photo: game
  },
  {
    title: "Valorant",
    url: "http://test4.com",
    photo: game
  }
];

// TODO: Fetch new ads from server
const ads = [
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 1000
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 2000
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 3000
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 4000
  }
];

export default function Home() {
  return (
    <>
      <Header />
      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Популярные категории</h1>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
          {categories.concat(categories).map((category) => (
            <Card className="bg-background py-0 overflow-hidden">
              <a href={category.url}>
                <img src={category.photo} alt={`Photo of ${category.title}`} />
                <h4 className="text-center py-2 font-bold">{category.title}</h4>
              </a>
            </Card>
          ))}
        </div>
      </section>

      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Новые объявления</h1>
        <div className="flex flex-col gap-8">
          {ads.map((ad) => (
            <Card className="p-4 md:flex-row md:items-center justify-start">
              <img
                src={ad.photo}
                alt={ad.title}
                className="h-auto w-auto md:max-h-32"
              />
              <div className="text-wrap grow">
                <h4 className="mb-4 font-bold">{ad.title}</h4>
                <p className="text-xs md:text-sm">{ad.description}</p>
              </div>
              <div className="flex gap-4 items-center">
                <Button variant="secondary">{`${ad.price} Р`}</Button>
                <Button size="icon" variant="secondary" className="p-2">
                  <img src={favouriteIcon} alt="" />
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
