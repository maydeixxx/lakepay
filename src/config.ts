export const SERVER_URL = "http://localhost:8990/";
export const USERS_URL = SERVER_URL + "user_id/";
export const PROFILE_URL = SERVER_URL + "user_info";
export const ADD_AD_URL = SERVER_URL + "save_ad";
export const AUTH_URL = SERVER_URL + "auth/telegram/token";

import cs2 from "@/assets/cs2.webp";
import dota2 from "@/assets/dota2.jpg";
import deadlock from "@/assets/deadlock.jpg";
import fortnite from "@/assets/fortnite.jpg";
import pubg from "@/assets/pubg.jpg";
export const categories = [
  {
    title: "PUBG",
    name: "pubg",
    photo: pubg
  },
  {
    title: "CS2",
    name: "cs2",
    photo: cs2
  },
  {
    title: "Fortnite",
    name: "fortnite",
    photo: fortnite
  },
  {
    title: "Deadlock",
    name: "deadlock",
    photo: deadlock
  },
  {
    title: "Dota 2",
    name: "dota2",
    photo: dota2
  }
];
