import { ShoppingCart, Footprints, MessageCircle, Home, MoreHorizontal } from 'lucide-react';
import type { TaskCategory } from '../api/tasks';

export const TASK_CATEGORIES: { value: TaskCategory; icon: typeof ShoppingCart; labelKey: string }[] = [
  { value: 'GROCERY_SHOPPING', icon: ShoppingCart, labelKey: 'newTask.categories.grocery' },
  { value: 'WALKING_COMPANION', icon: Footprints, labelKey: 'newTask.categories.walking' },
  { value: 'CHATTING_COMPANIONSHIP', icon: MessageCircle, labelKey: 'newTask.categories.chatting' },
  { value: 'HOUSEHOLD_HELP', icon: Home, labelKey: 'newTask.categories.household' },
  { value: 'OTHER', icon: MoreHorizontal, labelKey: 'newTask.categories.other' },
];
