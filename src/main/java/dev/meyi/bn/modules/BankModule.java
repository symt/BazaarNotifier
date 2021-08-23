package dev.meyi.bn.modules;

import dev.meyi.bn.utilities.Defaults;
import org.json.JSONObject;

public class BankModule extends Module {

  public BankModule() {
    super();
  }

  public BankModule(JSONObject config) {
    super(config);
  }

  @Override
  protected void draw() {

  }

  @Override
  protected void reset() {
    x = Defaults.BANK_MODULE_X;
    y = Defaults.BANK_MODULE_Y;
  }

  @Override
  protected String name() {
    return ModuleName.BANK.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return false;
  }

  @Override
  protected int getMaxShift() {
    return 0;
  }
}
