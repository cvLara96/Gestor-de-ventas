package com.example.ventasandusers.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ventasandusers.R;

import java.util.ArrayList;

public class MyAdapterArticulos extends RecyclerView.Adapter<MyAdapterArticulos.ViewHolderDatos> {

    //ESTE ADAPTADOR RECIBIRA UNA LISTA DE DATOS, DE MANERA QUE CREAMOS UN ARRAYLIST
    ArrayList<Articulo> listaArticulos;

    //CREAMOS UN LAYOUTINFLATER QUE INFLARA LA VISTA QUE TENDRA QUE MOSTRAR (la definida en list_pokemon.xml):
    private LayoutInflater inflater;

    //CREAMOS UN CONTEXT PARA INDICAR DE QUE CLASE ESTAMOS LLAMANDO ESTE ADAPTADOR
    private Context context;

    //CREAMOS UNA INSTANCIA DE HomeFragment
    GalleryFragment galleryFragment;


    //Creamos objetos necesarios
    Articulo articulo;

    //CREAREMOS EL CONSTRUCTOR
    public MyAdapterArticulos(ArrayList<Articulo> listaArticulos, Context context,GalleryFragment galleryFragment) {

        this.listaArticulos = listaArticulos;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.galleryFragment = galleryFragment;
    }

    //Vamos a crear otro constructor sin el Homefragment para el fragmento de registros:
    public MyAdapterArticulos(ArrayList<Articulo> listaArticulos, Context context) {

        this.listaArticulos = listaArticulos;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    //ESTE METODO ENLAZA EL ADAPTADOR CON EL FICHERO itemsrecycler.xml
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //DE MANERA QUE AQUI GENERAREMOS UN VIEW INFLADO CON ESE LAYOUT:
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lista_articulos, null, false);

        return new ViewHolderDatos(view);
    }

    //ESTE METODO SE ENCARGARA DE ESTABLECER LA COMUNICACION ENTRE NUESTRO ADAPTADOR Y
    //LA CLASE ViewHolderDatos
    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        //UTILIZAREMOS EL holder Y CREAREMOS UN METODO LLAMADO asignarDatos QUE
        //RECIBIRA COMO PARAMETRO LA INFORMACION QUE QUEREMOS QUE MUESTRE:
        //EL METODO asignarDatos debera estar creado en la clase ViewHolderDatos
        //DE MANERA QUE PULSAREMOS SOBRE EL PARA QUE LO GENERE EN ESTA CLASE:
        articulo = listaArticulos.get(position);
        holder.asignarDatos(articulo);

        final int posicionArticulo = position; // Variable final local que almacena la posición del jugador

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryFragment.addArticulo(posicionArticulo);
            }
        });



    }

    //ESTE METODO RETORNARA EL TAMAÑO DE LA LISTA
    @Override
    public int getItemCount() {
        return listaArticulos.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        //AQUI REFERENCIAMOS LOS ELEMENTOS QUE TENDRA EL RECYCLERVIEW
        TextView textoNombre;
        TextView precio;
        ImageView imagenArticulo;
        RelativeLayout relativeLayout;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relativePulsable);
            //PARA REFERENCIARLO USAMOS EL itemView:
            textoNombre = itemView.findViewById(R.id.textNombre);
            precio = itemView.findViewById(R.id.textPrecio);
            imagenArticulo = itemView.findViewById(R.id.imagenArticulo);
        }

        public void asignarDatos(Articulo articulo) {
            //UNA VEZ AQUI LE INDICAREMOS QUE ESTABLEZCA EN EL TEXTO EL STRING QUE RECIBE
            textoNombre.setText(articulo.getNombre());
            precio.setText(String.valueOf(articulo.getPrecion())+"€");
            imagenArticulo.setImageResource(articulo.getImagenArticulo());

        }
    }

//--> UNA VEZ CREADO EL ADAPTADOR PASAREMOS AL PASO 13 EN MAINACTIVITY2
}
