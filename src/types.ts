export type LoadingStatus = "idle" | "loading" | "succeeded" | "failed";

export type GameCategories = "pubg" | "cs2" | "fortnite" | "deadlock" | "dota2";

export type GameCategory = {
  title: string;
  name: GameCategories;
  photo: string;
};

export type User = {
  id: number;
  username: string;
  urlPhoto: string;
  dateOfReg: string;
  // For personal profile
  tgId?: number;
  balance?: number;
  subscriptions?: string[];
  roles?: string[];
};

export interface Product {
  id: number;
  title: string;
  body: string;
  category: GameCategories;
  price: number;
  quantity: number;
  dateOfPush: string;
  sold: boolean;
  sellerId: number;
}

export interface ChatRoomUser {
  id: number;
  username: string;
  urlPhoto: string;
}

export interface ChatRoom {
  id: number;
  sender: ChatRoomUser;
  recipient: ChatRoomUser;
}

export interface ChatMessage {
  sender: ChatRoomUser;
  recipient: ChatRoomUser;
  content: string;
  date: string;
}

/**
 * The data received from Telegram when the user is authenticated.
 *
 * @see https://core.telegram.org/widgets/login#receiving-authorization-data
 */
export interface TelegramAuthData {
  id: number;
  first_name: string;
  auth_date: number;
  hash: string;
  last_name?: string;
  photo_url?: string;
  username?: string;
}

export interface LoginButtonProps {
  /**
   * The URL where the auth data from Telegram will be sent.
   */
  authCallbackUrl?: string;

  /**
   * The username of the bot that will be used to authenticate the user.
   */
  botUsername: string;

  /**
   * The size of the button.
   *
   * @default 'large'
   */
  buttonSize?: "large" | "medium" | "small";

  /**
   * The radius of the button corners.
   */
  cornerRadius?: number;

  /**
   * The language of the button.
   *
   * @default "en"
   */
  lang?: string;

  /**
   * The callback function that will be called when the user is authenticated.
   */
  onAuthCallback?: (data: TelegramAuthData) => void;

  /**
   * The access level that the bot will request.
   *
   * @default "write"
   */
  requestAccess?: "write" | null;

  /**
   * Whether to show the user's avatar.
   *
   * @default true
   */
  showAvatar?: boolean;

  /**
   * The version of the Telegram widget to deal with browser caching.
   */
  widgetVersion?: number | string;
}

/**
 * The options to create the script.
 */
export interface CreateScriptOptions extends LoginButtonProps {}

/**
 * @internal
 */
export type TTelegramAuthLogin = Pick<LoginButtonProps, "onAuthCallback">;

declare global {
  interface Window {
    TelegramAuthLogin: TTelegramAuthLogin;
  }
}
