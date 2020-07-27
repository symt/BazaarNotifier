package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MouseHandler {

  @SubscribeEvent
  public void mouseActionCheck(TickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      if (BazaarNotifier.inBazaar) {
        BazaarNotifier.modules.movementCheck();
      }
    }
  }
}
