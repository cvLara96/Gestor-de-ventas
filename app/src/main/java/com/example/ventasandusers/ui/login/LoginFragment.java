package com.example.ventasandusers.ui.login;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ventasandusers.DatabaseHelper;
import com.example.ventasandusers.R;
import com.example.ventasandusers.Usuario;
import com.example.ventasandusers.databinding.FragmentLoginBinding;
import com.example.ventasandusers.ui.gallery.GalleryFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    //Boton de cerrar sesion
    Button cerrarSesion;

    //Boton reset
    Button reset;

    //Creamos un recyclerView que mostrara todos los usuarios que hay creados
    RecyclerView recyclerViewUsers;

    //Creamos un adaptador
    MyAdapterUsers myAdapterUsers;

    //Gestion de base de datos
    DatabaseHelper dbHelper, dbHelperVentas;
    SQLiteDatabase dbWriter, dbReader, dbWriterVentas;
    boolean permisoUsuario = false;
    boolean permisoAdmin = false;

    //listado de usuarios, lo haremos estatico, lo necesitamos en otro fragmento
    public static ArrayList<Usuario> usuariosRegistrados = new ArrayList<Usuario>();

    public static Usuario usuarioActivo = new Usuario();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LoginViewModel loginViewModel =
                new ViewModelProvider(this).get(LoginViewModel.class);

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cerrarSesion=binding.btnClose;
        cerrarSesion.setOnClickListener(this::logout);
        reset = binding.buttonReset;
        reset.setOnClickListener(this::resetDatabase);

        recyclerViewUsers = binding.recyclerUsers;

        dbHelper = new DatabaseHelper(getActivity(),"GestorUsuarios",null,1);
        dbWriter = dbHelper.getWritableDatabase();
        dbWriter.execSQL("CREATE TABLE IF NOT EXISTS users(nombre VARCHAR, password VARCHAR, permiso VARCHAR);");

        dbHelperVentas = new DatabaseHelper(getActivity(),"Gestor_Ventas",null,1);
        dbWriterVentas = dbHelperVentas.getWritableDatabase();

        //Buscaremos en la base de datos y si no esta añadido el usuario admin, lo añadira, de manera que siempre debe estar este usuario en la base de datos
        dbReader = dbHelper.getReadableDatabase();

        addAdminArray();
        addAdminDatabase();
        actualizarRecycler();

        return root;

    }

    //Metodo para resetear database
    private void resetDatabase(View v){

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Resetear base de datos")
                .setMessage("¿Estás seguro de que quieres borrar todos los datos?")
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            //Utilizamos el metodo .delete
                            //Pasaremos a la clausula where el numero de telefono que es unico para cada persona
                            //y no habra riesgo de eliminar dos contactos iguales
                            dbWriterVentas.delete("ventas",null, null);
                            //Otra forma tambien valida:
                            //dbWriter.delete("favoritos","numTelefono = ?", new String[]{contacto.getTelefono()});
                            Toast.makeText(getContext(), "Base de datos reseteada", Toast.LENGTH_SHORT).show();

                            //Reseteamos para que no choque con otra nueva alineacion
                            GalleryFragment.seleccionados.clear();
                            GalleryFragment.fechaSeleccionada = null;

                        }catch(SQLException e){
                            Toast.makeText(getContext(), "ERROR en el borrado", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();

    }


    //Metodo para cerrar sesion
    public void logout(View v){

        // Obtener la referencia al NavigationView
        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);

        // Obtener los IDs de los elementos que deseas deshabilitar
        int idItemGallery = R.id.nav_gallery;
        int idItemHome = R.id.nav_home;
        int idItemSlideshow = R.id.nav_slideshow;
        int idItemHistoric = R.id.nav_historic;

        // Deshabilitar el acceso a los elementos del menú
        MenuItem itemGallery = navigationView.getMenu().findItem(idItemGallery);
        MenuItem itemHome = navigationView.getMenu().findItem(idItemHome);
        MenuItem itemSlideshow = navigationView.getMenu().findItem(idItemSlideshow);
        MenuItem itemHistoric = navigationView.getMenu().findItem(idItemHistoric);

        if (itemGallery != null) {
            itemGallery.setVisible(false);
        }

        if (itemHome != null) {
            itemHome.setVisible(false);
        }

        if (itemSlideshow != null) {
            itemSlideshow.setVisible(false);
        }

        if (itemHistoric != null) {
            itemHistoric.setVisible(false);
        }

        permisoUsuario = false;
        permisoAdmin = false;
        reset.setVisibility(View.INVISIBLE);

    }

    //Metodo para cuando pulsemos sobre un usuario:
    public void usuarioCheck(int position) {

        Usuario usuario = usuariosRegistrados.get(position);

        if(permisoAdmin==true || permisoUsuario == true){
            Toast.makeText(getContext(), "Ya hay una sesion iniciada", Toast.LENGTH_SHORT).show();
        }else{
            //mostrara el dialogo enviando el la informacion del usuario seleccionado
            mostrarDialogo(usuario);
        }

    }

    //Dialogo
    private void mostrarDialogo(Usuario usuario) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.login_admin, null);

        final EditText etNombreUsuario = dialogView.findViewById(R.id.editTextNombreUsuario);
        etNombreUsuario.setText(usuario.getNombre());
        final EditText etPassword = dialogView.findViewById(R.id.editTextPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle(getBoldText("LOGIN"))
                .setPositiveButton(getBoldText("INICIAR SESION"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Validar el nombre de usuario y la contraseña
                        String passIngresada = etPassword.getText().toString();
                        verificarCredenciales(usuario.getNombre(), passIngresada);

                    }
                })
                .setNegativeButton(getBoldText("CANCELAR"), null)
                .show();
    }

    //Metodo para verificar credenciales
    public void verificarCredenciales(String nombre, String password){

        //Buscaremos en la base de datos por nombre y contraseña:

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT * FROM users WHERE nombre =\'"+ nombre +"\' AND password = \'" + password +"\'",null);
            if(cursor.getCount()==0){
                //Si no encuentra coincidencias:
                Toast.makeText(getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }else{

                //Si lo hace, habilitara los permisos dependiendo del usuario que se haya logueado

                while(cursor.moveToNext()){
                    String permiso = cursor.getString(2);
                    if(permiso.equals("admin")){
                        Toast.makeText(requireContext(), "Acceso de administrador concedido", Toast.LENGTH_SHORT).show();
                        permisoAdmin = true;
                        opcionesAdmin();

                    }else{
                        Toast.makeText(requireContext(), "Acceso de usuario concedido", Toast.LENGTH_SHORT).show();
                        permisoUsuario = true;
                        usuarioActivo.setNombre(nombre);
                        opcionesUser();
                    }
                }

            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "No existen articulos para ese filtro", Toast.LENGTH_SHORT).show();
        }

    }

    //Opciones de administrador
    public void opcionesAdmin(){
        // Obtener la referencia al NavigationView
        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);

        // Obtener los IDs de los elementos que deseas deshabilitar
        int idItemGallery = R.id.nav_gallery;
        int idItemHome = R.id.nav_home;
        int idItemSlideshow = R.id.nav_slideshow;
        int idItemHistoric = R.id.nav_historic;

        // Deshabilitar el acceso a los elementos del menú
        MenuItem itemGallery = navigationView.getMenu().findItem(idItemGallery);
        MenuItem itemHome = navigationView.getMenu().findItem(idItemHome);
        MenuItem itemSlideshow = navigationView.getMenu().findItem(idItemSlideshow);
        MenuItem itemHistoric = navigationView.getMenu().findItem(idItemHistoric);


        if (itemGallery != null) {
            itemGallery.setVisible(false);
        }

        if (itemHome != null) {
            itemHome.setVisible(true);
        }

        if (itemSlideshow != null) {
            itemSlideshow.setVisible(false);
        }

        if (itemHistoric != null) {
            itemHistoric.setVisible(false);
        }

        reset.setVisibility(View.VISIBLE);



    }

    //Opciones de usuario
    public void opcionesUser(){
        // Obtener la referencia al NavigationView
        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);

        // Obtener los IDs de los elementos que deseas deshabilitar
        int idItemGallery = R.id.nav_gallery;
        int idItemHome = R.id.nav_home;
        int idItemSlideshow = R.id.nav_slideshow;
        int idItemHistoric = R.id.nav_historic;

        // Deshabilitar el acceso a los elementos del menú
        MenuItem itemGallery = navigationView.getMenu().findItem(idItemGallery);
        MenuItem itemHome = navigationView.getMenu().findItem(idItemHome);
        MenuItem itemSlideshow = navigationView.getMenu().findItem(idItemSlideshow);
        MenuItem itemHistoric = navigationView.getMenu().findItem(idItemHistoric);


        if (itemGallery != null) {
            itemGallery.setVisible(true);
        }

        if (itemHome != null) {
            itemHome.setVisible(false);
        }

        if (itemSlideshow != null) {
            itemSlideshow.setVisible(true);
        }

        if (itemHistoric != null) {
            itemHistoric.setVisible(true);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dbReader.close();
        dbHelper.close();
        dbWriter.close();
        dbHelperVentas.close();
        dbWriterVentas.close();
    }

    //Metodo que añade por defecto al usuario administrador al arrayList y a la base de datos
    public void addAdminArray(){

        Usuario admin = new Usuario("admin", "1234", "admin");

        if(!usuariosRegistrados.contains(admin)){
            usuariosRegistrados.add(admin);
        }
        actualizarRecycler();

    }

    public void addAdminDatabase(){

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT * FROM users WHERE nombre = \'admin\'",null);
            if(cursor.getCount()==0){

                //Si no existe, lo añadira a la base de datos
                ContentValues values = new ContentValues();
                values.put("nombre", "admin");
                values.put("password", "1234");
                values.put("permiso", "admin");

                dbWriter.insert("users", null, values);

            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "Admin no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo para actualizar el recycler
    public void actualizarRecycler(){

        usuariosRegistrados.clear();

        //Leera la base de datos y actualizara con toda la info:
        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT * FROM users",null);
            if(cursor.getCount()==0){
                Toast.makeText(getContext(), "Base de datos vacia", Toast.LENGTH_SHORT).show();
            }else{

                while(cursor.moveToNext()){

                    Usuario usuario = new Usuario();
                    usuario.setNombre(cursor.getString(0));
                    usuario.setContraseña(cursor.getString(1));
                    usuario.setPermiso(cursor.getString(2));

                    if(!usuariosRegistrados.contains(usuario)){
                        usuariosRegistrados.add(usuario);
                    }

                }
            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "Admin no encontrado", Toast.LENGTH_SHORT).show();
        }

        //Pasamos la informacion al recyclerview
        myAdapterUsers = new MyAdapterUsers(usuariosRegistrados, getContext(), this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUsers.setAdapter(myAdapterUsers);

    }

    //Configuramos onResume para que cuando se abra la app no se muestre ninguna opcion del navigation hasta que nos logueemos:
    @Override
    public void onResume() {
        super.onResume();

        if(permisoAdmin == true){
            opcionesAdmin();
        }else if (permisoUsuario == true){
            opcionesUser();
        }else{
            // Obtener la referencia al NavigationView
            NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);

            // Obtener los IDs de los elementos que deseas deshabilitar
            int idItemGallery = R.id.nav_gallery;
            int idItemHome = R.id.nav_home;
            int idItemSlideshow = R.id.nav_slideshow;
            int idItemHistoric = R.id.nav_historic;

            // Deshabilitar el acceso a los elementos del menú
            MenuItem itemGallery = navigationView.getMenu().findItem(idItemGallery);
            MenuItem itemHome = navigationView.getMenu().findItem(idItemHome);
            MenuItem itemSlideshow = navigationView.getMenu().findItem(idItemSlideshow);
            MenuItem itemHistoric = navigationView.getMenu().findItem(idItemHistoric);

            if (itemGallery != null) {
                itemGallery.setVisible(false);
            }

            if (itemHome != null) {
                itemHome.setVisible(false);
            }

            if (itemSlideshow != null) {
                itemSlideshow.setVisible(false);
            }

            if (itemHistoric != null) {
                itemHistoric.setVisible(false);
            }
        }

    }

    //Texto en negrita
    private SpannableString getBoldText(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

}