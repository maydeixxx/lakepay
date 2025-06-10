import {
  ADS_ALL_URL,
  ADS_CATEGORY_URL,
  ADS_PERSONAL_URL,
  USERS_URL
} from "@/config";
import { useAppSelector } from "@/redux/store";
import type { Product } from "@/types";
import { useCallback, useEffect, useRef, useState } from "react";

// Fetch all products by default
export interface ProductHookProps {
  category?: string;
  personal?: boolean;
}

export function useProducts({ category, personal }: ProductHookProps) {
  const [products, setProducts] = useState<Product[] | null>([]);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const abortControllerRef = useRef<AbortController | null>(null);

  const { token } = useAppSelector((state) => state.auth);

  const constructURL = () => {
    if (personal) return ADS_PERSONAL_URL;
    if (category) return `${ADS_CATEGORY_URL}/${category}`;
    return ADS_ALL_URL;
  };

  const fetchProducts = useCallback(async () => {
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    const authHeaders = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    };

    try {
      const response = await fetch(constructURL(), {
        signal: abortControllerRef.current?.signal,
        headers: personal ? authHeaders : {}
      });

      const products = (await response.json()) as Product[];
      setProducts(products);
    } catch (e: any) {
      if (e.name === "SyntaxError") {
        setProducts([]);
      }

      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }

      setError(e);
    } finally {
      setIsLoading(false);
    }
  }, [category, personal]);

  useEffect(() => {
    fetchProducts();
  }, [category, personal]);

  return {
    products,
    error,
    isLoading,
    invalidate: fetchProducts
  };
}
