package com.example.ventasandusers.ui.gallery;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ventasandusers.R;
import com.example.ventasandusers.databinding.FragmentGalleryBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    //Articulos
    //-------------------

    //Creamos el recyclerView
    RecyclerView recyclerView;

    //Adaptador
    MyAdapterArticulos myAdapterArticulos;

    //Botones
    Button btnConsulta, btnFecha, btnDelete;


    //Creamos la lista de articulos
    ArrayList<Articulo> articulos = new ArrayList<>();
    public static ArrayList<Articulo>seleccionados = new ArrayList<>();
    public static String fechaSeleccionada = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnConsulta = binding.btnConsulta;
        btnConsulta.setOnClickListener(this::consultar);

        btnDelete = binding.btnBorrar;
        btnDelete.setOnClickListener(this::deleteSeleccionados);

        btnFecha = binding.btnFecha;
        btnFecha.setOnClickListener(this::selectFecha);



        recyclerView = binding.recyclerArticulos;

        agregarArticulos();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void agregarArticulos(){

        if(articulos.isEmpty()){
            Articulo articulo1 = new Articulo("Articulo 1", 2.50, R.drawable.caja);
            Articulo articulo2 = new Articulo("Articulo 2", 0.50, R.drawable.caja);
            Articulo articulo3 = new Articulo("Articulo 3", 2.00, R.drawable.caja);
            Articulo articulo4 = new Articulo("Articulo 4", 24.00, R.drawable.caja);
            Articulo articulo5 = new Articulo("Articulo 5", 1.75, R.drawable.caja);
            Articulo articulo6 = new Articulo("Articulo 6", 3.90, R.drawable.caja);
            Articulo articulo7 = new Articulo("Articulo 7", 6.55, R.drawable.caja);
            Articulo articulo8 = new Articulo("Articulo 8", 8.40, R.drawable.caja);
            Articulo articulo9 = new Articulo("Articulo 9", 55.00, R.drawable.caja);
            Articulo articulo10 = new Articulo("Articulo 10", 23.99, R.drawable.caja);
            Articulo articulo11 = new Articulo("Articulo 11", 2.70, R.drawable.caja);
            Articulo articulo12 = new Articulo("Articulo 12", 1.90, R.drawable.caja);
            Articulo articulo13 = new Articulo("Articulo 13", 4.40, R.drawable.caja);
            Articulo articulo14 = new Articulo("Articulo 14", 7.70, R.drawable.caja);
            Articulo articulo15 = new Articulo("Articulo 15", 46.55, R.drawable.caja);

            articulos.add(articulo1);
            articulos.add(articulo2);
            articulos.add(articulo3);
            articulos.add(articulo4);
            articulos.add(articulo5);
            articulos.add(articulo6);
            articulos.add(articulo7);
            articulos.add(articulo8);
            articulos.add(articulo9);
            articulos.add(articulo10);
            articulos.add(articulo11);
            articulos.add(articulo12);
            articulos.add(articulo13);
            articulos.add(articulo14);
            articulos.add(articulo15);
        }

        //Pasamos la informacion al recyclerview
        myAdapterArticulos = new MyAdapterArticulos(articulos, getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myAdapterArticulos);

    }

    //Metodo para los articulos seleccionados
    public void addArticulo(int posicionArticulo){

        if(!seleccionados.contains(articulos.get(posicionArticulo))){
            seleccionados.add(articulos.get(posicionArticulo));
        }

    }

    //Metodos para los botones
    private void selectFecha(View v){

        Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int anio, int mes, int dia) {
                        fechaSeleccionada = dia + "/" + (mes + 1) + "/" + anio;
                        Toast.makeText(getContext(), "Fecha " + dia + "/" + (mes + 1) + "/" + anio +" seleccionada", Toast.LENGTH_SHORT).show();
                    }
                },
                anio, mes, dia
        );
        datePickerDialog.show();

    }

    private void deleteSeleccionados(View v){
        seleccionados.clear();
        fechaSeleccionada = null;
    }

    private void consultar(View v){

        String mensaje = "";
        String fecha;

        if(fechaSeleccionada == null){
            fecha = "-- No se ha seleccionado ninguna fecha";
        }else{
            fecha = fechaSeleccionada;
        }

        if(seleccionados.isEmpty()){
            mensaje = "-- No hay articulos seleccionados --";
        }else{
            for(Articulo a : seleccionados){
                mensaje = mensaje + "\n" + a.getNombre();
            }
        }


        //Creamos un objeto de la calse AlertDialog para usar sus subclases
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Crearemos un titulo con .set Ttile de la siguiente forma:
        builder.setTitle("            ARTICULOS SELECCIONADOS");

        //Ahora con .setMessage establecemos el mensaje que aparecera en el dialogo:
        builder.setMessage(mensaje + "\n" + "Fecha: "+ fecha);

        builder.show();

    }
}