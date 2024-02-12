package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.handlers.MouseHandler;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ModuleList extends ArrayList<Module> {

  private Module movingModule = null;

  public ModuleList() {
     add(BazaarNotifier.config.suggestionModule);
     add(BazaarNotifier.config.bankModule);
     add(BazaarNotifier.config.notificationModule);
     add(BazaarNotifier.config.craftingModule);
  }

  public void rescaleCheck() {
    for (Module m : this) {
      if (m.inMovementBox() && Keyboard.isKeyDown(29) && !Keyboard.isKeyDown(42)) {
        float newScale = m.getScale() + (float) MouseHandler.mouseWheelMovement / 20;
        m.setScale(Math.max(newScale, 0.1f),false);
      }
    }
  }

  public void shiftSettingCheck() {
    for (Module m : this) {
      if (MouseHandler.mouseWheelMovement != 0 && Keyboard.isKeyDown(42) && !Keyboard
          .isKeyDown(29)) {
        m.mouseWheelShift -= MouseHandler.mouseWheelMovement;
        if (m.mouseWheelShift < 1) {
          m.mouseWheelShift = 1;
        }
      }
    }
  }

  public void movementCheck() {
    if (Mouse.isButtonDown(0)) {
      if (movingModule == null) {
        for (Module m : this) {
          if (m.inMovementBox()) {
            m.needsToMove = true;
          }
        }
        for (Module m : this) {
          if (movingModule != null) {
            m.needsToMove = false;
          } else if (m.needsToMove) {
            movingModule = m;
          }
        }
      }
      if (movingModule != null) {
        movingModule.handleMovement();
      }
    } else {
      if (movingModule != null) {
        movingModule.needsToMove = false;
        movingModule.moving = false;
      }
      movingModule = null;
    }
  }

  public void pageFlipCheck() {
    if (!Keyboard.isKeyDown(29) && !Keyboard.isKeyDown(42)) {
      if (MouseHandler.mouseWheelMovement != 0) {
        for (Module m : this) {
          if (m.inMovementBox() && m.getMaxShift() > 0) {
            m.shift += MouseHandler.mouseWheelMovement;
            if (m.shift > m.getMaxShift()) {
              m.shift = m.getMaxShift();
            } else if (m.shift < 0) {
              m.shift = 0;
            }

            break;
          }
        }
      }
    }
  }
  public void drawAllGui(){
    if (BazaarNotifier.config.enabled && BazaarNotifier.activeBazaar) {
      for (Module m : this) {
        if (m.shouldShowGui()) {
          m.position.setSize(m.getModuleWidth(), m.getModuleHeight());
          m.draw();
        }
      }
    }
  }


  public void resetAll() {
    for (Module m : this) {
      m.reset();
    }
  }

  public void resetScale() {
    for (Module m : this) {
      m.setScale(1, false);
    }
  }


}
