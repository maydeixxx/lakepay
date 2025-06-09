// TODO: Fetch popular categories from server
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { FavouriteIcon } from "@/icons/FavouriteIcon";
import { NavLink } from "react-router";
import { ALL_ADS_URL, categories } from "@/config";
import { ProductView } from "@/components/ProductView";
import { useEffect, useRef, useState } from "react";
import type { Product } from "@/types";
import { photoFromCategory } from "@/utils";

export default function Home() {
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [products, setProducts] = useState<Product[] | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    const fetchProducts = async () => {
      abortControllerRef.current?.abort();
      abortControllerRef.current = new AbortController();

      setIsLoading(true);

      try {
        const response = await fetch(ALL_ADS_URL, {
          signal: abortControllerRef.current?.signal
        });
        const products = (await response.json()) as Product[];
        setProducts(products);
      } catch (e: any) {
        if (e.name === "AbortError") {
          console.log("Aborted");
          return;
        }

        setError(e);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProducts();
  }, []);

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
          {products &&
            products.slice(0, 5).map((product) => (
              <ProductView
                key={product.id}
                show_favourite={true}
                product={{
                  id: product.id,
                  title: product.title,
                  description: product.body,
                  price: product.price,
                  photo: photoFromCategory(product.category),
                  favourite: false
                }}
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
