import { Button } from "@/components/Button";
import { NavLink, useParams } from "react-router";
import cartIcon from "@/assets/cart.svg";
import { useEffect, useRef, useState } from "react";
import type { Product, User } from "@/types";
import { ADS_URL, USERS_URL } from "@/config";
import UserIcon from "@/icons/UserIcon";
import { photoFromCategory } from "@/utils";
import SpinnerIcon from "@/icons/SpinnerIcon";

export default function ProductPage() {
  let { productId } = useParams<{ productId?: string }>();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [product, setProduct] = useState<Product | null>(null);
  const [seller, setSeller] = useState<User | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    const fetchProducts = async () => {
      abortControllerRef.current?.abort();
      abortControllerRef.current = new AbortController();

      setIsLoading(true);

      try {
        const response = await fetch(`${ADS_URL}/${productId}`, {
          signal: abortControllerRef.current?.signal
        });
        const product = (await response.json()) as Product;
        setProduct(product);
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

  useEffect(() => {
    const fetchSeller = async () => {
      abortControllerRef.current?.abort();
      abortControllerRef.current = new AbortController();

      setIsLoading(true);

      try {
        const response = await fetch(`${USERS_URL}/${product?.sellerId}`, {
          signal: abortControllerRef.current?.signal
        });
        const seller = (await response.json()) as User;
        setSeller(seller);
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

    if (product) {
      fetchSeller();
    }
  }, [product]);

  if (isLoading) {
    return (
      <>
        <section className="container mx-auto flex w-full justify-center h-96 items-center">
          <SpinnerIcon className="text-primary size-16" />
        </section>
      </>
    );
  }

  return (
    <>
      <section className="container mx-auto py-16 flex flex-col">
        <h1 className="text-secondary text-4xl mb-12">{product?.title}</h1>
        <div className="flex gap-8 mb-12 flex-wrap md:flex-nowrap">
          <img
            src={photoFromCategory(product?.category || "cs2")}
            className="rounded-xl md:basis-1/2 w-full h-full"
          />
          <div className="flex flex-col gap-4 md:basis-1/2">
            <h2 className="text-secondary text-3xl w-full">
              Цена: {product?.price}Р
            </h2>
            <div className="flex gap-4 py-4">
              <Button variant="secondary">
                <img src={cartIcon} alt="" />
              </Button>
              <Button variant="secondary">Купить сейчас</Button>
            </div>
            <NavLink
              to={`/user/${seller?.id}`}
              className="flex items-center gap-4"
            >
              {seller?.urlPhoto ? (
                <img
                  className="size-16 rounded-xl border-secondary border-2"
                  src={seller.urlPhoto}
                />
              ) : (
                <UserIcon className="text-secondary" />
              )}
              <p className="text-secondary">@{seller?.username}</p>
            </NavLink>
            <p className="text-secondary">В наличии: {product?.quantity}</p>
            <p className="text-secondary">Опубликован: {product?.dateOfPush}</p>
          </div>
        </div>
        <h2 className="text-secondary text-3xl mb-12">Описание:</h2>
        <p className="text-secondary text-justify">{product?.body}</p>
      </section>
    </>
  );
}
