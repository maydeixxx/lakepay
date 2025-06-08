import game from "@/assets/cs2.webp";
import { Button } from "@/components/Button";
import { Card } from "@/components/Card";
import { FavouriteIcon } from "@/icons/FavouriteIcon";

// TODO: Fetch favourite ads from server
const ads = [
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 1000,
    favourite: true
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 2000,
    favourite: true
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 3000,
    favourite: true
  },
  {
    title: "Название товара",
    description: "Lorem Ipsum is simply dummy text of the...",
    photo: game,
    price: 4000,
    favourite: true
  }
];

export default function FavouritesPage() {
  return (
    <>
      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Избранные объявления</h1>
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
                {/* Add on click handler with a confirmation popup to delete from favourites */}
                <Button size="icon" variant="secondary" className="p-2">
                  <FavouriteIcon
                    className={`${ad.favourite ? "text-destructive" : "text-secondary-foreground"}`}
                  />
                </Button>
              </div>
            </Card>
          ))}
        </div>
      </section>
    </>
  );
}
