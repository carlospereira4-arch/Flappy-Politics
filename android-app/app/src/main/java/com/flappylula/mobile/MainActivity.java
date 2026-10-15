package com.flappylula.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity implements FlappyLulaView.MenuListener {
    private static final int PEDIDO_FOTO_PERFIL = 401;
    private static final String ARQUIVO_FOTO_PENDENTE = "foto_perfil_pendente.jpg";

    private ProgressoJogo progresso;
    private FlappyLulaView jogo;
    private AlertDialog dialogoPerfil;
    private EditText campoNome;
    private EditText campoIdade;
    private TextView avisoFoto;

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        progresso = new ProgressoJogo(this);
        jogo = new FlappyLulaView(this, progresso, this);
        setContentView(jogo);
        jogo.setFotoPerfil(carregarFotoPerfil());
        if (!progresso.temPerfil()) {
            getWindow().getDecorView().post(() -> abrirCadastroPerfil(true));
        }
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

    @Override
    public void editarPerfil() {
        abrirCadastroPerfil(false);
    }

    private void abrirCadastroPerfil(boolean obrigatorio) {
        if (!obrigatorio) {
            arquivoFotoRascunho().delete();
            jogo.setFotoPerfil(carregarFotoPerfil());
        }
        LinearLayout formulario = new LinearLayout(this);
        formulario.setOrientation(LinearLayout.VERTICAL);
        int margem = (int) (24 * getResources().getDisplayMetrics().density);
        formulario.setPadding(margem, margem / 2, margem, 0);

        campoNome = new EditText(this);
        campoNome.setSingleLine(true);
        campoNome.setHint("Nome de usuário");
        campoNome.setText(progresso.getNomeUsuario());
        campoNome.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        formulario.addView(campoNome);

        campoIdade = new EditText(this);
        campoIdade.setSingleLine(true);
        campoIdade.setHint("Idade");
        campoIdade.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (progresso.getIdade() > 0) {
            campoIdade.setText(String.valueOf(progresso.getIdade()));
        }
        formulario.addView(campoIdade);

        Button escolherFoto = new Button(this);
        escolherFoto.setText("ESCOLHER FOTO DE PERFIL");
        escolherFoto.setOnClickListener(v -> abrirGaleria());
        formulario.addView(escolherFoto);

        avisoFoto = new TextView(this);
        avisoFoto.setText(temFotoPerfil() ? "Foto selecionada" : "Selecione uma foto para continuar");
        formulario.addView(avisoFoto);

        AlertDialog.Builder construtor = new AlertDialog.Builder(this)
                .setTitle(obrigatorio ? "CRIAR PERFIL LOCAL" : "EDITAR PERFIL")
                .setMessage("Perfil local: sem e-mail e sem conta on-line. Um nome novo cria outro perfil; reutilize o nome para recuperar os dados salvos.")
                .setView(formulario)
                .setPositiveButton(obrigatorio ? "CRIAR PERFIL" : "SALVAR", null);
        if (!obrigatorio) {
            construtor.setNegativeButton("CANCELAR", null);
        }

        dialogoPerfil = construtor.create();
        dialogoPerfil.setCancelable(!obrigatorio);
        dialogoPerfil.setCanceledOnTouchOutside(!obrigatorio);
        dialogoPerfil.setOnShowListener(dialog -> dialogoPerfil.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> salvarPerfil()));
        dialogoPerfil.setOnCancelListener(dialog -> {
            if (!obrigatorio) {
                arquivoFotoRascunho().delete();
                jogo.setFotoPerfil(carregarFotoPerfil());
            }
        });
        dialogoPerfil.show();
        if (!obrigatorio) {
            dialogoPerfil.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                arquivoFotoRascunho().delete();
                jogo.setFotoPerfil(carregarFotoPerfil());
                dialogoPerfil.dismiss();
            });
        }
        // AlertDialog exibe a mensagem acima do formulário; o ajuste evita que os campos
        // fiquem espremidos em aparelhos com tela pequena.
        dialogoPerfil.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void salvarPerfil() {
        String nome = campoNome.getText().toString().trim();
        String idadeTexto = campoIdade.getText().toString().trim();
        if (nome.isEmpty() || nome.length() > 20) {
            campoNome.setError("Informe um nome de 1 a 20 caracteres");
            return;
        }
        int idade;
        try {
            idade = Integer.parseInt(idadeTexto);
        } catch (NumberFormatException e) {
            campoIdade.setError("Informe sua idade");
            return;
        }
        if (idade < 1 || idade > 120) {
            campoIdade.setError("Use uma idade entre 1 e 120");
            return;
        }
        if (!temFotoPerfil()) {
            Toast.makeText(this, "Escolha uma foto de perfil para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }
        File fotoAnterior = arquivoFotoPerfil();
        String idAnterior = progresso.getIdPerfil();
        progresso.salvarPerfil(nome, idade);
        File fotoAtual = arquivoFotoPerfil();
        File rascunho = arquivoFotoRascunho();
        if (rascunho.isFile()) {
            copiarArquivo(rascunho, fotoAtual);
            rascunho.delete();
        } else if (!idAnterior.equals(progresso.getIdPerfil()) && !fotoAtual.exists()
                && fotoAnterior.isFile()) {
            copiarArquivo(fotoAnterior, fotoAtual);
        }
        jogo.setFotoPerfil(carregarFotoPerfil());
        jogo.atualizarDadosDoPerfil();
        dialogoPerfil.dismiss();
    }

    private void abrirGaleria() {
        android.content.Intent intencao = new android.content.Intent(
                android.content.Intent.ACTION_OPEN_DOCUMENT);
        intencao.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intencao.setType("image/*");
        try {
            startActivityForResult(intencao, PEDIDO_FOTO_PERFIL);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Não foi possível abrir a galeria deste aparelho.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int pedido, int resultado, android.content.Intent dados) {
        super.onActivityResult(pedido, resultado, dados);
        if (pedido != PEDIDO_FOTO_PERFIL || resultado != RESULT_OK || dados == null
                || dados.getData() == null) {
            return;
        }
        try {
            Bitmap foto = copiarFotoParaArmazenamentoLocal(dados.getData());
            jogo.setFotoPerfil(foto);
            if (avisoFoto != null) {
                avisoFoto.setText("Foto selecionada");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Não consegui carregar essa imagem. Tente outra foto.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap copiarFotoParaArmazenamentoLocal(android.net.Uri uri) throws Exception {
        BitmapFactory.Options dimensoes = new BitmapFactory.Options();
        dimensoes.inJustDecodeBounds = true;
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(entrada, null, dimensoes);
        }
        int maiorLado = Math.max(dimensoes.outWidth, dimensoes.outHeight);
        int amostragem = 1;
        while (maiorLado / amostragem > 512) {
            amostragem *= 2;
        }
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = amostragem;
        Bitmap foto;
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            foto = BitmapFactory.decodeStream(entrada, null, opcoes);
        }
        if (foto == null) {
            throw new IllegalArgumentException("Imagem inválida");
        }
        File destino = arquivoFotoRascunho();
        try (FileOutputStream saida = new FileOutputStream(destino)) {
            foto.compress(Bitmap.CompressFormat.JPEG, 88, saida);
            saida.flush();
        }
        return foto;
    }

    private Bitmap carregarFotoPerfil() {
        File rascunho = arquivoFotoRascunho();
        File arquivo = rascunho.isFile() ? rascunho : arquivoFotoPerfil();
        if (!arquivo.exists()) {
            return null;
        }
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = 2;
        return BitmapFactory.decodeFile(arquivo.getAbsolutePath(), opcoes);
    }

    private boolean temFotoPerfil() {
        return arquivoFotoRascunho().isFile() || arquivoFotoPerfil().isFile();
    }

    private File arquivoFotoRascunho() {
        return new File(getFilesDir(), "foto_perfil_selecao.jpg");
    }

    private File arquivoFotoPerfil() {
        String idPerfil = progresso.getIdPerfil();
        String nomeArquivo = idPerfil.isEmpty() ? ARQUIVO_FOTO_PENDENTE
                : "foto_perfil_" + idPerfil + ".jpg";
        return new File(getFilesDir(), nomeArquivo);
    }

    private void copiarArquivo(File origem, File destino) {
        try (InputStream entrada = new java.io.FileInputStream(origem);
             FileOutputStream saida = new FileOutputStream(destino)) {
            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
            }
            saida.flush();
        } catch (Exception e) {
            Toast.makeText(this, "Não consegui copiar a foto para esse perfil.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (jogo != null && jogo.voltarAoMenu()) {
            return;
        }
        super.onBackPressed();
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
