package com.example.ventasandusers.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ventasandusers.DatabaseHelper;
import com.example.ventasandusers.R;
import com.example.ventasandusers.Usuario;
import com.example.ventasandusers.databinding.FragmentHomeBinding;
import com.example.ventasandusers.ui.login.LoginFragment;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    //Desde este fragmento el administrador podra crear usuarios pulsando un boton, o borrarlos pulsando sobre el usuario

    //Boton de crear usuarios
    Button crearUsuarios;

    //Creamos un recycler
    RecyclerView recyclerView;

    //Creamos el adaptador (sera el mismo salvo que este nos mostrara toda la informacion del usuario registrado)
    MyAdapterUsersAdmin myAdapterUsersAdmin;

    //Array de usuarios creados
    ArrayList<Usuario> creados = new ArrayList<Usuario>();

    //GESTION DE BASE DE DATOS
    DatabaseHelper dbHelper;
    SQLiteDatabase dbWriter, dbReader;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        recyclerView = binding.recyclerView;
        crearUsuarios = binding.btnCreate;
        crearUsuarios.setOnClickListener(this::crearUsuario);

        dbHelper = new DatabaseHelper(getActivity(), "GestorUsuarios", null, 1);
        dbWriter = dbHelper.getWritableDatabase();
        dbReader = dbHelper.getReadableDatabase();

        actualizarRecycler();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dbHelper.close();
        dbWriter.close();
    }

    public void crearUsuario(View view) {

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.create_user, null);

        final EditText etNombreUsuario = dialogView.findViewById(R.id.editTextNombreUsuario);
        final EditText etPassword = dialogView.findViewById(R.id.editTextPasswordCreate);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle(getBoldText("CREAR NUEVO USUARIO"))
                .setPositiveButton(getBoldText("CONFIRMAR"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Validar el nombre de usuario y la contraseña
                        String usuarioIngresado = etNombreUsuario.getText().toString();
                        String passIngresada = etPassword.getText().toString();
                        creacion(usuarioIngresado, passIngresada);

                    }
                })
                .setNegativeButton(getBoldText("CANCELAR"), null)
                .show();

    }

    //Metodo que añade el usuario a la base de datos, al array de usuarios registrados y al array de este fragmento
    public void creacion(String nombre, String password) {

        //Añadir a los arrays
        Usuario nuevoUsuario = new Usuario(nombre, password, "user");

        //Lo buscara en la base de datos y si existe, no lo añadira:
        //Leera la base de datos y actualizara con toda la info:
        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT * FROM users WHERE nombre = \'" + nombre + "\'", null);
            if (cursor.getCount() == 0) {

                creados.add(nuevoUsuario);
                LoginFragment.usuariosRegistrados.add(nuevoUsuario);
                //Añadirlo a la base de datos
                ContentValues values = new ContentValues();
                values.put("nombre", nuevoUsuario.getNombre());
                values.put("password", nuevoUsuario.getContraseña());
                values.put("permiso", nuevoUsuario.getPermiso());

                dbWriter.insert("users", null, values);

                Toast.makeText(getContext(), "Usuario añadido con exito", Toast.LENGTH_SHORT).show();
                actualizarRecycler();


            } else {

                while (cursor.moveToNext()) {
                    Toast.makeText(getContext(), "Este usuario ya esta registrado, elige otro nombre", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (SQLException e) {
            Toast.makeText(getContext(), "Admin no encontrado", Toast.LENGTH_SHORT).show();
        }


    }

    //Texto en negrita
    private SpannableString getBoldText(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    //Metodo para actualizar el recycler
    public void actualizarRecycler() {

        creados.clear();

        //Leera la base de datos y actualizara con toda la info:
        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT * FROM users WHERE permiso != \'admin\'", null);
            if (cursor.getCount() == 0) {
                Toast.makeText(getContext(), "Base de datos vacia", Toast.LENGTH_SHORT).show();
            } else {

                while (cursor.moveToNext()) {

                    Usuario usuario = new Usuario();
                    usuario.setNombre(cursor.getString(0));
                    usuario.setContraseña(cursor.getString(1));
                    usuario.setPermiso(cursor.getString(2));

                    if (!creados.contains(usuario)) {
                        creados.add(usuario);
                    }

                }
            }

        } catch (SQLException e) {
            Toast.makeText(getContext(), "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
        }


        //Pasamos la informacion al recyclerview
        myAdapterUsersAdmin = new MyAdapterUsersAdmin(creados, getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myAdapterUsersAdmin);

    }

    //Metodo que sera llamado cuando se pulse sobre un elemento del recycler
    public void deleteUser(Usuario usuario) {

        try {

            //Utilizamos el metodo .delete
            //Pasaremos a la clausula where el numero de telefono que es unico para cada persona
            //y no habra riesgo de eliminar dos contactos iguales
            dbWriter.delete("users", "nombre = " + "\'" + usuario.getNombre() + "\'", null);
            //Otra forma tambien valida:
            //dbWriter.delete("favoritos","numTelefono = ?", new String[]{contacto.getTelefono()});
            Toast.makeText(getContext(), "El usuario " + usuario.getNombre() + " ha sido eliminado de la base de datos", Toast.LENGTH_SHORT).show();
            actualizarRecycler();

        } catch (SQLException e) {
            Toast.makeText(getContext(), "ERROR en el borrado", Toast.LENGTH_SHORT).show();
        }

    }

}