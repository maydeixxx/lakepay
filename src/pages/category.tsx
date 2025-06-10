import { Card } from "@/components/Card";
import { ProductView } from "@/components/ProductView";
import { ADS_CATEGORY_URL, categories } from "@/config";
import type { Product } from "@/types";
import { photoFromCategory } from "@/utils";
import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate, useParams } from "react-router";

export default function CategoryPage() {
  let { categoryId } = useParams<{ categoryId?: string }>();
  const category = categories.find((c) => c.name == categoryId);

  const navigate = useNavigate();
  if (!category) {
    navigate("/not_found");
  }

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
        const response = await fetch(`${ADS_CATEGORY_URL}/${categoryId}`, {
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
        <h1 className="text-secondary text-4xl mb-8">
          Категория {category!.title}
        </h1>
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
        </div>
      </section>
    </>
  );
}
