import { Button } from "@/components/Button";
import { NavLink, useNavigate, useParams } from "react-router";
import { useEffect, useRef, useState } from "react";
import type { Product, User } from "@/types";
import { ADS_URL, BUY_AD_URL, USERS_URL } from "@/config";
import UserIcon from "@/icons/UserIcon";
import { photoFromCategory } from "@/utils";
import SpinnerIcon from "@/icons/SpinnerIcon";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { CartIcon } from "@/icons/CartIcon";
import { TrashIcon } from "@/icons/TrashIcon";
import { addToCart, removeFromCart } from "@/features/cart/cartActions";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@/components/Dialog";
import { DialogDescription } from "@radix-ui/react-dialog";

export default function ProductPage() {
  let { productId } = useParams<{ productId?: string }>();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [product, setProduct] = useState<Product | null>(null);
  const [seller, setSeller] = useState<User | null>(null);
  const { user, token } = useAppSelector((state) => state.auth);
  const [confirmationOpen, setConfirmationOpen] = useState<boolean>(false);

  const abortControllerRef = useRef<AbortController | null>(null);
  const dispatch = useAppDispatch();
  const cart = useAppSelector((state) => state.cart);
  const navigate = useNavigate();

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

  if (isLoading || !product) {
    return (
      <>
        <section className="container mx-auto flex w-full justify-center h-96 items-center">
          <SpinnerIcon className="text-primary size-16" />
        </section>
      </>
    );
  }

  async function buyProduct() {
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    if (!user) {
      navigate("/login");
      return;
    }

    try {
      const response = await fetch(`${BUY_AD_URL}`, {
        method: "post",
        signal: abortControllerRef.current?.signal,
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          userId: user?.id,
          adId: product?.id
        })
      });

      if (response.ok) {
      }
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }
      console.error(e);
    }
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
              Цена: {product?.price}$
            </h2>
            {user && seller?.id != user.id && (
              <div className="flex gap-4 py-4">
                {cart.items[product?.id] ? (
                  <Button
                    variant="secondary"
                    onClick={() => dispatch(removeFromCart(product!))}
                  >
                    <TrashIcon />
                  </Button>
                ) : (
                  <Button
                    variant="secondary"
                    onClick={() => dispatch(addToCart(product!))}
                  >
                    <CartIcon />
                  </Button>
                )}

                <Dialog
                  open={confirmationOpen}
                  onOpenChange={(open) => {
                    setConfirmationOpen(open);
                  }}
                >
                  <DialogTrigger asChild>
                    <Button onClick={buyProduct} variant="secondary">
                      Купить сейчас
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="gap-8 w-full sm:max-w-2xl">
                    <DialogHeader>
                      <DialogTitle>Подтвердите действие</DialogTitle>
                      <DialogDescription>
                        Вы уверены что хотите совершить покупку?
                      </DialogDescription>
                    </DialogHeader>
                    <div className="flex gap-4">
                      <DialogClose asChild>
                        <Button variant="destructive">Отмена</Button>
                      </DialogClose>
                      <Button variant="primary" onClick={buyProduct}>
                        Подтвердить
                      </Button>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            )}
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
