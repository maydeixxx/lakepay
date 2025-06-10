import { Button } from "@/components/Button";
import { Card } from "@/components/Card";
import { ProductView } from "@/components/ProductView";
import {
  ADS_CATEGORY_URL,
  categories,
  SUBSCRIBE_URL,
  UNSUBSCRIBE_URL
} from "@/config";
import { useProducts } from "@/hooks/useProducts";
import { useUser } from "@/hooks/useUser";
import { useAppSelector } from "@/redux/store";
import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate, useParams } from "react-router";

export default function CategoryPage() {
  let { categoryId } = useParams<{ categoryId?: string }>();
  const category = categories.find((c) => c.name == categoryId);
  const { token, user: userInfo } = useAppSelector((state) => state.auth);
  const { products } = useProducts({ category: categoryId });
  const { user, invalidate: invalidateUser } = useUser(userInfo?.id || 0);

  const navigate = useNavigate();
  useEffect(() => {
    if (!category) {
      navigate("/not_found");
    }
  }, [categoryId]);

  const abortControllerRef = useRef<AbortController | null>(null);

  async function subscribeToCategory() {
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    try {
      const response = await fetch(`${SUBSCRIBE_URL}`, {
        method: "put",
        body: JSON.stringify({
          id: user?.id,
          category: category?.name
        }),
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        signal: abortControllerRef.current?.signal
      });

      if (response.ok) {
        invalidateUser();
        console.log("Подписка успешна");
      }
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }
    }
  }

  async function unsubscribeFromCategory() {
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    try {
      const response = await fetch(`${UNSUBSCRIBE_URL}`, {
        method: "put",
        body: JSON.stringify({
          id: user?.id,
          category: category?.name
        }),
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        signal: abortControllerRef.current?.signal
      });

      if (response.ok) {
        invalidateUser();
        console.log("Отписка успешна");
      }
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }
    }
  }

  return (
    <>
      <section className="container mx-auto py-16">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-secondary text-4xl">
            Категория {category!.title}
          </h1>
          {token &&
            (user?.subscriptions?.find((sub) => sub == category?.name) ? (
              <Button
                variant="primary"
                className="text-sm"
                onClick={unsubscribeFromCategory}
              >
                Отписаться
              </Button>
            ) : (
              <Button
                variant="primary"
                className="text-sm"
                onClick={subscribeToCategory}
              >
                Подписаться
              </Button>
            ))}
        </div>
        <div className="flex flex-col gap-8">
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
        </div>
      </section>
    </>
  );
}
