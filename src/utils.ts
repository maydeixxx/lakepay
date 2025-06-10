import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import type { GameCategories } from "./types";
import { categories } from "./config";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function photoFromCategory(category: GameCategories): string {
  return categories.find((c) => c.name == category.toUpperCase())?.photo || "";
}
