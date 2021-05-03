package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MouseHandler {

  int tick = 0;
  boolean inPageFlip = false;

  @SubscribeEvent
  public void mouseActionCheck(TickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      if (BazaarNotifier.inBazaar) {
        BazaarNotifier.modules.movementCheck();
        if (tick >= 8 && !inPageFlip) { // 2.5 times per second
          inPageFlip = true;
          BazaarNotifier.modules.pageFlipCheck();
          tick = 0;
          inPageFlip = false;
        }
        tick++;
      } else {
        tick = 0;
      }
    }
  }
}
