package com.flappylula.mobile;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private FlappyLulaView jogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        jogo = new FlappyLulaView(this);
        setContentView(jogo);
        esconderBarrasDoSistema();
    }

    @Override
    protected void onResume() {
        super.onResume();
        esconderBarrasDoSistema();
        if (jogo != null) {
            jogo.retomar();
        }
    }

    @Override
    protected void onPause() {
        if (jogo != null) {
            jogo.pausar();
        }
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean temFoco) {
        super.onWindowFocusChanged(temFoco);
        if (temFoco) {
            esconderBarrasDoSistema();
        }
    }

    @SuppressWarnings("deprecation")
    private void esconderBarrasDoSistema() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
}
