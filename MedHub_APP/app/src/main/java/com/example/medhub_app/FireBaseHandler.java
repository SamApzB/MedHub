package com.example.medhub_app;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireBaseHandler {



    public static void getHorasMedico(String uid,Date fechaElegida){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference medicoRef = db.collection("Medicos").document(uid);
        medicoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                int horaInicio = documentSnapshot.getLong("horaInicio").intValue();
                int horaFin = documentSnapshot.getLong("horaFin").intValue();

                // Continuar con el siguiente paso
                consultarCitasDisponibles(fechaElegida, horaInicio, horaFin, uid, db);
            } else {
                // Manejar el error
            }
        }).addOnFailureListener(e -> {
            // Manejar el error
        });
    }
    public static void consultarCitasDisponibles(Date fechaElegida, int horaInicio, int horaFin, String medicoId, FirebaseFirestore db) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaElegida);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date inicioDia = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date finDia = calendar.getTime();

        Query query = db.collection("Medicos").document(medicoId).collection("citas")
                .whereGreaterThanOrEqualTo("fechaHoraInicio", inicioDia)
                .whereLessThanOrEqualTo("fechaHoraFin", finDia);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Date> horasReservadas = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Date fechaHoraInicio = document.getDate("fechaHoraInicio");
                horasReservadas.add(fechaHoraInicio);
            }

            List<Date> horasDisponibles = obtenerHorasDisponibles(fechaElegida, horaInicio, horaFin, horasReservadas);
            // Mostrar estas horas en la interfaz de usuario






        }).addOnFailureListener(e -> {
            // Manejar el error
        });
    }

    public static List<Date> obtenerHorasDisponibles(Date fechaElegida, int horaInicio, int horaFin, List<Date> horasReservadas) {
        List<Date> horasDisponibles = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaElegida);
        calendar.set(Calendar.HOUR_OF_DAY, horaInicio);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        while (calendar.get(Calendar.HOUR_OF_DAY) < horaFin) {
            Date horaActual = calendar.getTime();
            if (!horasReservadas.contains(horaActual)) {
                horasDisponibles.add(horaActual);
            }
            calendar.add(Calendar.MINUTE, 30);  // Incrementa en 30 minutos
        }

        return horasDisponibles;
    }

    public static void addDataToMedico(String contacto, String dni, String especialidad, int horaFin, int horaInicio, String nombres, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("contacto", contacto);
        data.put("dni", dni);
        data.put("especialidad", especialidad);
        data.put("horaFin", horaFin);
        data.put("horaInicio", horaInicio);
        data.put("nombres", nombres);
        data.put("uid", uid);

        db.collection("Medicos").document(uid)  // Usando el uid como el ID del documento
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Acciones a realizar si la operación fue exitosa, por ejemplo, mostrar un Toast o actualizar la UI.
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Acciones a realizar en caso de fallo, como mostrar un mensaje de error.
                    }
                });
    }
    private void getDataFromMedico(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Medicos").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String contacto = document.getString("contacto");
                                String dni = document.getString("dni");
                                String especialidad = document.getString("especialidad");
                                int horaFin = document.getLong("horaFin").intValue(); // Firestore almacena números enteros como Long por defecto
                                int horaInicio = document.getLong("horaInicio").intValue();
                                String nombres = document.getString("nombres");
                                String uidObtenido = document.getString("uid"); // Es opcional si ya tienes el uid desde el argumento.

                            } else {

                            }
                        } else {
                            // Acciones a realizar en caso de fallo, como mostrar un mensaje de error.
                        }
                    }
                });
    }







}
