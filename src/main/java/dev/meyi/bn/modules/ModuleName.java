package dev.meyi.bn.modules;

public enum ModuleName {
  SUGGESTION, BANK, NOTIFICATION, CRAFTING;

  public Module returnDefaultModule() {
    switch (this) {
      case SUGGESTION:
        return new SuggestionModule();
      case BANK:
        return new BankModule();
      case NOTIFICATION:
        return new NotificationModule();
      case CRAFTING:
        return new CraftingModule();
    }
    return null;
  }
}
