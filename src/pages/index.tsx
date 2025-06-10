// TODO: Fetch popular categories from server
import { Card } from "@/components/Card";
import { NavLink } from "react-router";
import { ProductView } from "@/components/ProductView";
import { useAppSelector } from "@/redux/store";
import { useProducts } from "@/hooks/useProducts";
import { categories } from "@/config";
import { Loading } from "@/components/Loading";

export default function Home() {
  const { products, error, isLoading } = useProducts({});
  const { user } = useAppSelector((state) => state.auth);

  return (
    <>
      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Популярные категории</h1>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
          {categories.map((category) => (
            <Card
              key={category.name}
              className="bg-background py-0 overflow-hidden"
            >
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
          {isLoading && <Loading />}
          {products &&
            products
              .slice(0, 5)
              .map((product) => (
                <ProductView
                  key={product.id}
                  personal={user?.id == product.sellerId}
                  product={product}
                />
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
