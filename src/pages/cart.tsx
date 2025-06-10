import { ProductView } from "@/components/ProductView";
import { useAppSelector } from "@/redux/store";
import { useEffect } from "react";
import { useNavigate } from "react-router";

export default function CartPage() {
  const { items } = useAppSelector((state) => state.cart);
  const { user } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate("/login");
    }
  }, [user]);

  return (
    <>
      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Ваша корзина:</h1>
        {Object.values(items).map((product) => (
          <ProductView product={product} />
        ))}
      </section>
    </>
  );
}
