package com.example.pathfide.Fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pathfide.R
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Typeface
import com.example.pathfide.Utils.PaymentNotificationHelper

data class Payment(
    val clientId: String = "",
    val therapistId: String = "",
    val amountPaid: String = "",
    val paymentTimestamp: String = "",
    val paymentProofUrl: String = "",
    val status: String = ""
)

class AdminPanelFragment : Fragment() {
    private lateinit var paymentTableLayout: TableLayout
    private lateinit var pageCounterTextView: TextView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private val db = FirebaseFirestore.getInstance()

    private val paymentsPerPage = 10
    private var currentPage = 1
    private var allPayments = mutableListOf<Pair<String, Payment>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_panel, container, false)

        paymentTableLayout = view.findViewById(R.id.paymentTableLayout)
        pageCounterTextView = view.findViewById(R.id.pageCounterTextView)
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)

        previousButton.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                displayCurrentPage()
            }
        }

        nextButton.setOnClickListener {
            if (currentPage * paymentsPerPage < allPayments.size) {
                currentPage++
                displayCurrentPage()
            }
        }

        fetchPayments()

        return view
    }

    private fun fetchPayments() {
        db.collection("payments")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { result ->
                allPayments.clear()
                for (document in result) {
                    val payment = document.toObject(Payment::class.java)
                    allPayments.add(Pair(document.id, payment))
                }
                updatePaginationControls()
                displayCurrentPage()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching payment data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayCurrentPage() {
        paymentTableLayout.removeAllViews()

        // Create header row
        val headerRow = TableRow(requireContext()).apply {
            setPadding(10, 10, 10, 10)
            setBackgroundColor(resources.getColor(R.color.navy_blue)) // Use the accent color for the background
        }

        // Create layout params for equal column widths
        var layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )

        // Create each header cell individually to ensure proper styling
        val headers = listOf(
            TextView(requireContext()).apply {
                text = "User ID"
                layoutParams = layoutParams
                setTextColor(resources.getColor(R.color.white))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            },
            TextView(requireContext()).apply {
                text = "Payment Timestamp"
                layoutParams = layoutParams
                setTextColor(resources.getColor(R.color.white))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            },
            TextView(requireContext()).apply {
                text = "Therapist ID"
                layoutParams = layoutParams
                setTextColor(resources.getColor(R.color.white))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            },
            TextView(requireContext()).apply {
                text = "Actions"
                layoutParams = layoutParams
                setTextColor(resources.getColor(R.color.white))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            }
        )

        // Add headers to header row
        headers.forEach { header ->
            headerRow.addView(header)
        }

        // Add the header row to the table
        paymentTableLayout.addView(headerRow)

        // Calculate start and end indices for current page
        val startIndex = (currentPage - 1) * paymentsPerPage
        val endIndex = minOf(startIndex + paymentsPerPage, allPayments.size)

        // Display current page items
        for (i in startIndex until endIndex) {
            val (documentId, payment) = allPayments[i]
            addPaymentRow(documentId, payment)
        }

        pageCounterTextView.text = "Page $currentPage"
        updatePaginationControls()
    }


    private fun addPaymentRow(documentId: String, payment: Payment) {
        val tableRow = TableRow(requireContext()).apply {
            setPadding(8, 8, 8, 8)
        }

        // Create layout params for equal column widths
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )

        // User ID column
        val userIdTextView = TextView(requireContext()).apply {
            text = payment.clientId
            this.layoutParams = layoutParams
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // Payment Timestamp column
        val timestampTextView = TextView(requireContext()).apply {
            text = payment.paymentTimestamp
            this.layoutParams = layoutParams
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // Therapist ID column
        val therapistIdTextView = TextView(requireContext()).apply {
            text = payment.therapistId
            this.layoutParams = layoutParams
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // Buttons container with layout params
        val buttonsContainer = LinearLayout(requireContext()).apply {
            this.layoutParams = layoutParams
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // Accept button
        val acceptButton = Button(requireContext()).apply {
            text = "Accept"
            setTextColor(resources.getColor(R.color.white))
            setBackgroundColor(resources.getColor(R.color.acceptButtonColor))
            setOnClickListener {
                updatePaymentStatus(documentId, "Payment Successful!")
            }
        }

        // Decline button
        val declineButton = Button(requireContext()).apply {
            text = "Decline"
            setTextColor(resources.getColor(R.color.white))
            setBackgroundColor(resources.getColor(R.color.declineButtonColor))
            setOnClickListener {
                updatePaymentStatus(documentId, "Payment Declined")
            }
        }

        // Add buttons to container
        buttonsContainer.addView(acceptButton)
        buttonsContainer.addView(declineButton)

        // Add all views to the row in correct order
        tableRow.apply {
            addView(userIdTextView)
            addView(timestampTextView)
            addView(therapistIdTextView)
            addView(buttonsContainer)
        }

        // Add row to table
        paymentTableLayout.addView(tableRow)
    }


    private fun updatePaymentStatus(documentId: String, status: String) {
        db.collection("payments").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val clientId = document.getString("clientId") ?: ""
                val therapistId = document.getString("therapistId") ?: ""

                // Update the payment status
                db.collection("payments").document(documentId)
                    .update("status", status)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()

                        // Send notifications to both user and therapist
                        PaymentNotificationHelper.sendPaymentStatusNotification(
                            clientId = clientId,
                            therapistId = therapistId,
                            status = status,
                            paymentId = documentId
                        )

                        fetchPayments()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error updating payment status", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching payment details", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updatePaginationControls() {
        previousButton.isEnabled = currentPage > 1
        nextButton.isEnabled = currentPage * paymentsPerPage < allPayments.size
    }
}