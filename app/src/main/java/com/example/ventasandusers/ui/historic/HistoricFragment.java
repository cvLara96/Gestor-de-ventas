package com.example.ventasandusers.ui.historic;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ventasandusers.DatabaseHelper;
import com.example.ventasandusers.R;
import com.example.ventasandusers.databinding.FragmentHistoricBinding;
import com.example.ventasandusers.databinding.FragmentLoginBinding;
import com.example.ventasandusers.ui.gallery.Articulo;
import com.example.ventasandusers.ui.gallery.GalleryFragment;
import com.example.ventasandusers.ui.gallery.MyAdapterArticulos;
import com.example.ventasandusers.ui.login.LoginFragment;
import com.example.ventasandusers.ui.login.LoginViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HistoricFragment extends Fragment {

    private FragmentHistoricBinding binding;

    //Registro
    //Aqui tendremos un spinner con los compradores registrados
    //Las fechas de cada comprador y los articulos que ha comprado en cada fecha

    //---------------------------------

    TextView textNumTelefono, textTotal;
    Spinner spinnerComprador, spinnerFecha;
    ImageView imagenContacto;

    Button send;

    RecyclerView recyclerView;

    //Gestion de base de datos
    DatabaseHelper dbHelper;
    SQLiteDatabase dbReader, dbWriter;

    ArrayList<String> compradores = new ArrayList<>();

    //Adaptador
    MyAdapterArticulos myAdapterArticulos;

    double precio = 0.0;

    String compradorSeleccionado = null;

    //Lo que enviaremos por mail:
    ArrayList<Articulo>articulosEmail = new ArrayList<>();
    String total = "";
    String fechaEnviar = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        HistoricViewModel historicViewModel =
                new ViewModelProvider(this).get(HistoricViewModel.class);

        binding = FragmentHistoricBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        send = binding.buttonSend;
        send.setOnClickListener(this::enviarMail);
        spinnerComprador = binding.spinnerComprador;
        spinnerFecha = binding.spinnerFecha;
        textTotal = binding.textTotal;
        textNumTelefono = binding.textNumTelefono;
        imagenContacto = binding.imagenComprador;
        recyclerView = binding.recyclerRegistro;

        //GESTION BASE DE DATOS
        dbHelper = new DatabaseHelper(getActivity(),"Gestor_Ventas",null,1);
        dbWriter = dbHelper.getWritableDatabase();
        dbReader = dbHelper.getReadableDatabase();



        obtenerCompradores();

        spinnerComprador.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                compradorSeleccionado = parent.getSelectedItem().toString();
                infoComprador(compradorSeleccionado);
                obtenerFechas(compradorSeleccionado, LoginFragment.usuarioActivo.getNombre());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerFecha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fechaSeleccionada = parent.getSelectedItem().toString();
                obtenerArticulos(compradorSeleccionado, fechaSeleccionada);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        textTotal.setText(String.valueOf(precio)+"€");

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dbHelper.close();
        dbReader.close();
    }


    //Metodos que llamaremos en los spinners
    private void obtenerCompradores(){

        compradores.clear();

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT comprador FROM ventas WHERE vendedor = \'" + LoginFragment.usuarioActivo.getNombre()+"\'",null);
            if(cursor.getCount()==0){
                Toast.makeText(getContext(), "No existen compradores en la base de datos", Toast.LENGTH_SHORT).show();
            }else{

                while(cursor.moveToNext()){

                    //Añadimos los compradores
                    if(!compradores.contains(cursor.getString(0))){
                        compradores.add(cursor.getString(0));
                    }

                }
                ArrayAdapter<String> adapterSpinnerComprador;
                adapterSpinnerComprador = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, compradores);
                spinnerComprador.setAdapter(adapterSpinnerComprador);
                cursor.close();
            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "No existen compradores guardados en la base de datos", Toast.LENGTH_SHORT).show();
        }

    }

    //Metodo para obtener la informacion del comprador que se seleccione en el spinner
    private void infoComprador(String comprador){

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT telefono, imagenComprador FROM ventas WHERE comprador = \'" + comprador + "\'",null);
            if(cursor.getCount()==0){
                Toast.makeText(getContext(), "No existen datos de ese comprador", Toast.LENGTH_SHORT).show();
            }else {

                while (cursor.moveToNext()) {

                    textNumTelefono.setText(cursor.getString(0));
                    if(cursor.getString(1)!=null){
                        Picasso.get().load(cursor.getString(1)).into(imagenContacto);
                    }else{
                        imagenContacto.setImageResource(R.drawable.contactoicon);
                    }
                }

                cursor.close();

            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "No existen datos de ese comprador", Toast.LENGTH_SHORT).show();
        }

    }

    //Metodo para obtener las fechas de compra de cada comprador
    private void obtenerFechas(String comprador, String vendedor){

        ArrayList<String>fechas = new ArrayList<String>();

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT fecha FROM ventas WHERE comprador = \'" + comprador + "\' AND vendedor = \'" + vendedor +"\'",null);
            if(cursor.getCount()==0){
                Toast.makeText(getContext(), "No existen fechas para ese comprador", Toast.LENGTH_SHORT).show();
            }else{

                while(cursor.moveToNext()){

                    if(!fechas.contains(cursor.getString(0))){
                        fechas.add(cursor.getString(0));
                    }

                }
                //Pasamos la informacion al spinner de fechas
                ArrayAdapter<String> adapterSpinnerFechas;
                adapterSpinnerFechas = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fechas);
                spinnerFecha.setAdapter(adapterSpinnerFechas);
                cursor.close();
            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "No existen fechas para ese comprador", Toast.LENGTH_SHORT).show();
        }

    }

    //Metodo que nos dara los articulos comprados por el comprador en la fecha seleccionada
    private void obtenerArticulos(String comprador, String fecha){

        ArrayList<Articulo> articulosFiltrados= new ArrayList<Articulo>();
        precio = 0.0;

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT nombreArticulo, precio FROM ventas WHERE comprador = \'" + comprador + "\' AND fecha = \'" + fecha + "\'",null);
            if(cursor.getCount()==0){
                Toast.makeText(getContext(), "No existen articulos para ese filtro", Toast.LENGTH_SHORT).show();
            }else{

                while(cursor.moveToNext()){

                    Articulo articulo = new Articulo();
                    articulo.setNombre(cursor.getString(0));
                    articulo.setPrecion(Double.parseDouble(cursor.getString(1)));
                    articulo.setImagenArticulo(R.drawable.caja);

                    if(!articulosFiltrados.contains(articulo)){
                        articulosFiltrados.add(articulo);
                    }

                }

                //Actualizamos la lista que enviaremos por mail
                actualizarLista(articulosFiltrados);

                //Actualizamos la fecha para enviar
                fechaEnviar = fecha;

                //Pasamos la informacion al recyclerview
                myAdapterArticulos = new MyAdapterArticulos(articulosFiltrados, getContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(myAdapterArticulos);

                //Recorremos los articulos filtrados, sumamos el precio y lo mostramos en textView
                for(Articulo a : articulosFiltrados){
                    precio += a.getPrecion();

                }
                textTotal.setText(String.valueOf(precio)+"€");

                //Actualizamos el total que enviaremos por mail
                total = String.valueOf(precio)+"€";

                cursor.close();
            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "No existen articulos para ese filtro", Toast.LENGTH_SHORT).show();
        }


    }

    //Metodo que obtiene el email del comprador al que queremos enviar
    private String obtenerEmail(String comprador){

        String emailComprador = "";

        ArrayList<Articulo> articulosFiltrados= new ArrayList<Articulo>();
        precio = 0.0;

        try {

            //Creamos un cursor con la query
            Cursor cursor = dbReader.rawQuery("SELECT emailComprador FROM ventas WHERE comprador = \'" + comprador + "\'",null);
            if(cursor.getCount()==0){
                Toast.makeText(getContext(), "No existe la informacion buscada", Toast.LENGTH_SHORT).show();
            }else{

                while (cursor.moveToNext()) {

                    if(cursor.getString(0)!=null){
                        emailComprador = cursor.getString(0);
                    }else{
                        emailComprador = "default_mail@gmail.com";
                    }
                }

                cursor.close();

            }

        }catch(SQLException e){
            Toast.makeText(getContext(), "No existen articulos para ese filtro", Toast.LENGTH_SHORT).show();
        }

        return emailComprador;

    }

    //Metodo que actualiza la lista de elementos que se envia por correo
    private void actualizarLista(ArrayList<Articulo>articulosFiltrados){

        articulosEmail.clear();
        articulosEmail = articulosFiltrados;

    }

    //Metodo para enviar el email
    //Metodo para enviar el correo
    public void enviarMail (View v){

        String articulosComprados = "";

        //En primer lugar, se crea el objeto Intent:
        Intent i = new Intent();

        //Creamos un chooser y lo igualamos a null:
        //El chooser es el mecanismo por el cual Android permite al usuario elegir una aplicación de entre las
        //posibles candidatas a tratar la petición que envía el intent
        Intent chooser = null;

        //Asignamos la accion al intent implicito
        //ACTION_SEND -> (enviar)
        i.setAction(Intent.ACTION_SEND);
        //Le indicamos el valor de lo que debera buscar, al tratarse de un
        //email, indicaremos mailto: en el setData() y luego mediante .putExtra()
        //indicaremos el resto de campos:
        i.setData(Uri.parse("mailto:"));
        //Creamos el "para" del mail
        String para [] = {obtenerEmail(compradorSeleccionado)};
        //Creamos el asunto
        i.putExtra(Intent.EXTRA_EMAIL, para);
        i.putExtra(Intent.EXTRA_SUBJECT, "Información venta"); //Indicamos un asunto por defecto

        for(Articulo a : articulosEmail){
            articulosComprados = articulosComprados + " \n " + a.getNombre() + " -- Precio: " + a.getPrecion() + "€";
        }

        i.putExtra(Intent.EXTRA_TEXT, fechaEnviar + "\n" + articulosComprados + "\nTotal pedido: " + total); //Indicamos un mensaje por defecto

        //Para enviar un email hay
        //que indicar que el tipo corresponde al MIME especificado en la RFC 822 --> ("message/rfc822")
        i.setType("message/rfc822");
        //Creamos el chooser
        chooser = Intent.createChooser(i, "Enviar email");
        //Iniciamos la actividad, al no esperar un resultado, sera con startActivity()
        startActivity(i);
        //Lanzamos una tostada a modo de informacion:
        Toast.makeText(getContext(),"Envia el email!", Toast.LENGTH_LONG).show();
    }

}