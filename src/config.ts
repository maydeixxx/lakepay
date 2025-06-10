export const SERVER_URL = "https://lakepay.ru/";
export const USERS_URL = SERVER_URL + "user_id";
export const PROFILE_URL = SERVER_URL + "user_info";
export const AUTH_URL = SERVER_URL + "auth/telegram/token";

export const ADD_AD_URL = SERVER_URL + "save_ad";
export const ADS_ALL_URL = SERVER_URL + "all_ads";
export const ADS_CATEGORY_URL = SERVER_URL + "ad_category";
export const ADS_PERSONAL_URL = SERVER_URL + "personal_ads";
export const ADS_URL = SERVER_URL + "ad_id";
export const ADS_DELETE = SERVER_URL + "delete_my_ad";

export const CHAT_LIST = SERVER_URL + "chat/list";
export const CHAT_INFO = SERVER_URL + "chat/info";
export const CHAT_HISTORY = SERVER_URL + "chat/history";

export const BUY_AD_URL = SERVER_URL + "buy";
export const DEPOSIT_URL = SERVER_URL + "deposit";
export const WITHDRAW_URL = SERVER_URL + "withdraw";

export const SUBSCRIBE_URL = SERVER_URL + "subscribe";
export const UNSUBSCRIBE_URL = SERVER_URL + "unsubscribe";

export const WS_URL = SERVER_URL + "ws";

import cs2 from "@/assets/cs2.webp";
import dota2 from "@/assets/dota2.jpg";
import deadlock from "@/assets/deadlock.jpg";
import fortnite from "@/assets/fortnite.jpg";
import pubg from "@/assets/pubg.jpg";
import type { GameCategory } from "./types";
export const categories: GameCategory[] = [
  {
    title: "PUBG",
    name: "PUBG",
    photo: pubg
  },
  {
    title: "CS2",
    name: "CS2",
    photo: cs2
  },
  {
    title: "Fortnite",
    name: "FORTNITE",
    photo: fortnite
  },
  {
    title: "Deadlock",
    name: "DEADLOCK",
    photo: deadlock
  },
  {
    title: "Dota 2",
    name: "DOTA2",
    photo: dota2
  }
];
