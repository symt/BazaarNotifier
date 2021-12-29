package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;


public class MouseHandler {

  public static int mouseWheelMovement = 0;
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
          BazaarNotifier.modules.rescaleCheck();
          BazaarNotifier.modules.shiftSettingCheck();
          mouseWheel();

          tick = 0;
          inPageFlip = false;
        }
        tick++;
      } else {
        tick = 0;
      }
    }
  }


  public void mouseWheel() {
    mouseWheelMovement = (int) Math.round((double) Mouse.getDWheel() * -1 / 100);
  }
}
