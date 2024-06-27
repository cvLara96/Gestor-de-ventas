package com.example.ventasandusers.ui.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ventasandusers.R;
import com.example.ventasandusers.Usuario;

import java.util.ArrayList;

public class MyAdapterUsers extends RecyclerView.Adapter<MyAdapterUsers.ViewHolderDatos> {

    //ESTE ADAPTADOR RECIBIRA UNA LISTA DE DATOS, DE MANERA QUE CREAMOS UN ARRAYLIST
    ArrayList<Usuario> listaUsuarios;

    //CREAMOS UN LAYOUTINFLATER QUE INFLARA LA VISTA QUE TENDRA QUE MOSTRAR (la definida en list_pokemon.xml):
    private LayoutInflater inflater;

    //CREAMOS UN CONTEXT PARA INDICAR DE QUE CLASE ESTAMOS LLAMANDO ESTE ADAPTADOR
    private Context context;

    //CREAMOS UNA INSTANCIA DE HomeFragment
    LoginFragment loginFragment;


    //Creamos objetos necesarios
    Usuario usuario;

    //CREAREMOS EL CONSTRUCTOR
    public MyAdapterUsers(ArrayList<Usuario> listaUsuarios, Context context, LoginFragment loginFragment) {

        this.listaUsuarios = listaUsuarios;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.loginFragment = loginFragment;
    }

    @NonNull
    @Override
    //ESTE METODO ENLAZA EL ADAPTADOR CON EL FICHERO itemsrecycler.xml
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //DE MANERA QUE AQUI GENERAREMOS UN VIEW INFLADO CON ESE LAYOUT:
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_users, null, false);

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
        usuario = listaUsuarios.get(position);
        holder.asignarDatos(usuario);

        final int posicionArticulo = position; // Variable final local que almacena la posición del jugador

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginFragment.usuarioCheck(position);

            }
        });


    }

    //ESTE METODO RETORNARA EL TAMAÑO DE LA LISTA
    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        //AQUI REFERENCIAMOS LOS ELEMENTOS QUE TENDRA EL RECYCLERVIEW
        TextView textoNombre;
        View view;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.relativePulsable);
            //PARA REFERENCIARLO USAMOS EL itemView:
            textoNombre = itemView.findViewById(R.id.textName);
        }

        public void asignarDatos(Usuario usuario) {
            //UNA VEZ AQUI LE INDICAREMOS QUE ESTABLEZCA EN EL TEXTO EL STRING QUE RECIBE
            textoNombre.setText(usuario.getNombre());

        }
    }

//--> UNA VEZ CREADO EL ADAPTADOR PASAREMOS AL PASO 13 EN MAINACTIVITY2
}

