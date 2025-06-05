import { Header } from "@/components/Header";

// TODO: Fetch popular categories from server
import game from "@/assets/cs2-category.jpg";
import { Card } from "@/components/Card";
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

export default function Home() {
  return (
    <>
      <Header />
      <div className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Популярные категории</h1>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
          {categories.concat(categories).map((category) => (
            <Card className="bg-background py-0 overflow-hidden">
              <a href={category.url}>
                <img src={category.photo} alt={`Photo of ${category.title}`} />
                <h4 className="text-center py-2">{category.title}</h4>
              </a>
            </Card>
          ))}
        </div>
      </div>
    </>
  );
}
