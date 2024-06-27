package com.example.ventasandusers.ui.slideshow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
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
import com.example.ventasandusers.Contacto;
import com.example.ventasandusers.databinding.FragmentSlideshowBinding;
import com.example.ventasandusers.ui.gallery.Articulo;
import com.example.ventasandusers.ui.gallery.GalleryFragment;
import com.example.ventasandusers.ui.login.LoginFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    //Contactos y registro en base de datos
    //-------------------------------

    RecyclerView recyclerView;
    MyAdapterContacts adapterContacts;

    Button btnRegistrar;
    EditText buscador;

    public static ArrayList<Contacto> lista_contactos = new ArrayList<>();

    Contacto contacto = new Contacto();

    //Gestion base de datos
    DatabaseHelper dbHelper;
    SQLiteDatabase dbWriter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerContacts;

        //Gestionamos los permisos con Dexter
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(response.getPermissionName().equals(Manifest.permission.READ_CONTACTS)){
                            mostrarContactos();
                        }

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                }).check();

        //GESTION BASE DE DATOS
        dbHelper = new DatabaseHelper(getActivity(),"Gestor_Ventas",null,1);
        dbWriter = dbHelper.getWritableDatabase();
        dbWriter.execSQL("CREATE TABLE IF NOT EXISTS ventas(nombreArticulo VARCHAR, precio VARCHAR, fecha VARCHAR, comprador VARCHAR," +
                "telefono VARCHAR, imagenComprador VARCHAR, emailComprador VARCHAR ,vendedor VARCHAR);");

        //BOTON
        btnRegistrar = binding.btnRegistrar;
        btnRegistrar.setOnClickListener(this::registrar);

        //BUSCADOR
        buscador = binding.buscador;
        buscador.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                buscarContacto(buscador.getText().toString());

                return true;
            }
        });


        return root;
    }

    //Metodo que mostrara la lista de contactos
    @SuppressLint("Range")
    //Metodo que se encargara de obtener los contactos y añadirlos a un arrayList, el cual sera añadido al recycler
    public void mostrarContactos() {

        //Con este if lo que haremos sera que si ya se ha creado previamente la lista de contactos no sera necesario que realice
        //estos pasos, ya que puede haber cambios realizados y la resetearia
        if (lista_contactos.isEmpty()) {

            ContentResolver contentResolver = requireContext().getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);

            while (cursor.moveToNext()) {
                String nombre = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String numTelefono = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String fotoContacto = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                //Obtenemos el email
                // Obtener el ID del contacto
                String idContacto = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                Cursor emails = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{idContacto},
                        null
                );

                String email = "";
                if(emails != null && emails.moveToNext()){
                    email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    emails.close();
                }

                // Consulta para obtener la fecha de nacimiento
                Cursor birthdayCursor = getActivity().getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Event.START_DATE },
                        ContactsContract.Data.CONTACT_ID + " = ? AND " +
                                ContactsContract.Data.MIMETYPE + " = ? AND " +
                                ContactsContract.CommonDataKinds.Event.TYPE + " = ?",
                        new String[] { idContacto, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) },
                        null);

                // Variable para almacenar la fecha de nacimiento
                String fechaNacimiento = null;

                // Verificar si se encontró la fecha de nacimiento
                if (birthdayCursor != null && birthdayCursor.moveToFirst()) {
                    // Obtener la fecha de nacimiento del cursor
                    fechaNacimiento = birthdayCursor.getString(birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    birthdayCursor.close();
                }

                // Si la fecha de nacimiento es nula, asignar un valor predeterminado
                if (fechaNacimiento == null) {
                    fechaNacimiento = "--No dispone--";
                }

                Contacto contacto = new Contacto(nombre, numTelefono, fotoContacto, email,fechaNacimiento,false);
                lista_contactos.add(contacto);
                //Cerramos el cursor

            }
            cursor.close();

        }

        //Pasamos la informacion al recyclerview
        adapterContacts = new MyAdapterContacts(lista_contactos, getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapterContacts);
    }

    //Metodo para buscar contactos
    @SuppressLint("Range")
    private void buscarContacto(String nombre){

        lista_contactos.clear();

        if(nombre.equals("")){
            mostrarContactos();
        }else{

            ContentResolver contentResolver = requireContext().getContentResolver();
            //Filtro:
            String filtro = ContactsContract.Contacts.DISPLAY_NAME + " like?";
            String args_filtro[] = {"%" + nombre + "%"};

            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, filtro, args_filtro, null);

            while (cursor.moveToNext()) {
                String nombreContacto = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String numTelefono = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String fotoContacto = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                //Obtenemos el email
                // Obtener el ID del contacto
                String idContacto = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                Cursor emails = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{idContacto},
                        null
                );

                String email = "";
                if(emails != null && emails.moveToNext()){
                    email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    emails.close();
                }

                // Consulta para obtener la fecha de nacimiento
                Cursor birthdayCursor = getActivity().getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Event.START_DATE },
                        ContactsContract.Data.CONTACT_ID + " = ? AND " +
                                ContactsContract.Data.MIMETYPE + " = ? AND " +
                                ContactsContract.CommonDataKinds.Event.TYPE + " = ?",
                        new String[] { idContacto, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) },
                        null);

                // Variable para almacenar la fecha de nacimiento
                String fechaNacimiento = null;

                // Verificar si se encontró la fecha de nacimiento
                if (birthdayCursor != null && birthdayCursor.moveToFirst()) {
                    // Obtener la fecha de nacimiento del cursor
                    fechaNacimiento = birthdayCursor.getString(birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    birthdayCursor.close();
                }

                // Si la fecha de nacimiento es nula, asignar un valor predeterminado
                if (fechaNacimiento == null) {
                    fechaNacimiento = "--No dispone--";
                }

                Contacto contacto = new Contacto(nombreContacto, numTelefono, fotoContacto, email, fechaNacimiento, false);
                lista_contactos.add(contacto);
                //Cerramos el cursor

            }
            cursor.close();
            //Pasamos la informacion al recyclerview
            adapterContacts = new MyAdapterContacts(lista_contactos, getContext(), this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapterContacts);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dbHelper.close();
        dbWriter.close();
    }

    //Metodo para cuando se seleccione un contacto
    public void contactoSeleccionado(int position){

        contacto = new Contacto(
                lista_contactos.get(position).getNombre(),
                lista_contactos.get(position).getTelefono(),
                lista_contactos.get(position).getImagen(),
                lista_contactos.get(position).getEmail(),
                lista_contactos.get(position).getFechaNacimiento(),
                true
        );

    }

    //Metodo que registra la venta
    private void registrar (View v){

        if(GalleryFragment.seleccionados.isEmpty()){
            Toast.makeText(getContext(), "No hay articulos seleccionados", Toast.LENGTH_SHORT).show();
        }else if(GalleryFragment.fechaSeleccionada==null){
            Toast.makeText(getContext(), "No se ha seleccionado ninguna fecha", Toast.LENGTH_SHORT).show();
        }else if(contacto == null || contacto.getNombre() == null){
            Toast.makeText(getContext(), "Debes seleccionar un contacto para registrar la venta", Toast.LENGTH_SHORT).show();
        }else{

            for(Articulo a : GalleryFragment.seleccionados){

                ContentValues values = new ContentValues();
                values.put("nombreArticulo", a.getNombre());
                values.put("precio", a.getPrecion());
                values.put("fecha", GalleryFragment.fechaSeleccionada);
                values.put("comprador", contacto.getNombre());
                values.put("telefono", contacto.getTelefono());
                values.put("imagenComprador", contacto.getImagen());
                values.put("emailComprador", contacto.getEmail());
                values.put("vendedor", LoginFragment.usuarioActivo.getNombre());

                dbWriter.insert("ventas", null, values);

            }

            GalleryFragment.fechaSeleccionada = null;
            GalleryFragment.seleccionados.clear();
            Toast.makeText(getContext(), "Venta registrada", Toast.LENGTH_SHORT).show();

        }

    }
}