package com.example.ventasandusers.ui.slideshow;

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
import com.example.ventasandusers.Contacto;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapterContacts extends RecyclerView.Adapter<MyAdapterContacts.ViewHolderDatos> {

    //ESTE ADAPTADOR RECIBIRA UNA LISTA DE DATOS, DE MANERA QUE CREAMOS UN ARRAYLIST
    ArrayList<Contacto> listaContactos;

    //CREAMOS UN LAYOUTINFLATER QUE INFLARA LA VISTA QUE TENDRA QUE MOSTRAR (la definida en list_pokemon.xml):
    private LayoutInflater inflater;

    //CREAMOS UN CONTEXT PARA INDICAR DE QUE CLASE ESTAMOS LLAMANDO ESTE ADAPTADOR
    private Context context;

    //CREAMOS UNA INSTANCIA DE HomeFragment
    SlideshowFragment slideshowFragment;


    //Creamos objetos necesarios
    Contacto contacto;

    //CREAREMOS EL CONSTRUCTOR
    public MyAdapterContacts(ArrayList<Contacto> listaContactos, Context context, SlideshowFragment slideshowFragment) {

        this.listaContactos = listaContactos;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.slideshowFragment = slideshowFragment;
    }

    @NonNull
    @Override
    //ESTE METODO ENLAZA EL ADAPTADOR CON EL FICHERO itemsrecycler.xml
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //DE MANERA QUE AQUI GENERAREMOS UN VIEW INFLADO CON ESE LAYOUT:
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_contactos, null, false);

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
        contacto = listaContactos.get(position);
        holder.asignarDatos(contacto);

        final int posicionContacto = position; // Variable final local que almacena la posición del jugador


        //ESTE METODO SE LLAMARA CUANDO SE PULSE SOBRE LA ESTRELLA DE LOS CONTACTOS cuando esta apagada
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideshowFragment.contactoSeleccionado(position);
            }
        });

    }

    //ESTE METODO RETORNARA EL TAMAÑO DE LA LISTA
    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        //AQUI REFERENCIAMOS LOS ELEMENTOS QUE TENDRA EL RECYCLERVIEW
        TextView textoNombre;
        TextView textoTelefono;
        TextView textoEmail;
        TextView textoFecha;
        ImageView imagenContacto;
        RelativeLayout relativeLayout;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relativePulsable);
            //PARA REFERENCIARLO USAMOS EL itemView:
            textoNombre = itemView.findViewById(R.id.textNombre);
            textoTelefono = itemView.findViewById(R.id.textNumTelefono);
            textoEmail = itemView.findViewById(R.id.textEmail);
            textoFecha = itemView.findViewById(R.id.textFecha);
            imagenContacto = itemView.findViewById(R.id.imageCard);
        }

        public void asignarDatos(Contacto datosContacto) {
            //UNA VEZ AQUI LE INDICAREMOS QUE ESTABLEZCA EN EL TEXTO EL STRING QUE RECIBE
            textoNombre.setText(datosContacto.getNombre());
            textoTelefono.setText(datosContacto.getTelefono());
            textoEmail.setText(datosContacto.getEmail());
            textoFecha.setText(datosContacto.getFechaNacimiento());
            if(datosContacto.getImagen()!=null){
                Picasso.get().load(contacto.getImagen()).into(imagenContacto);
            }else{
                imagenContacto.setImageResource(R.drawable.contactoicon);
            }

        }
    }

//--> UNA VEZ CREADO EL ADAPTADOR PASAREMOS AL PASO 13 EN MAINACTIVITY2
}
