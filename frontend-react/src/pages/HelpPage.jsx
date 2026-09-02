import { getRole } from "../auth.js";
import Navbar from "../components/Navbar.jsx";

function HelpPage() {
  const role = getRole();

  return (
    <div>
      <Navbar />
      <div className="container my-4">
        <div className="uja-card">
          <h5>Help</h5>

          <h6 className="mt-4">Accessing the application</h6>
          <p>
            Unauthenticated visitors are directed to the login page. Users without an account can
            create one through the "Register here" link, providing their full name, a username, an
            email address, a password (minimum 6 characters), their profile, and their department.
            Administrator accounts cannot be self-registered.
          </p>

          {role === "TEACHER" && (
            <>
              <h6 className="mt-4">Working as a teacher</h6>
              <p>
                The dashboard shows your own purchase requests, newest first, ten per page — use
                the Previous / Next buttons below the table to see older requests. Each order shows
                its current status: PENDING, APPROVED, REJECTED, PROCESSED, or DELIVERED.
              </p>
              <p>
                To create a request, press "+ New Order". Search for the expenditure centre by
                typing its name or code, fill in the line items (product, quantity, price, VAT
                rate, supplier), and press "Sign and submit" — this applies your personal digital
                signature to the generated document.
              </p>
              <p>
                While an order remains PENDING you can edit it; once decided, it is locked. The
                Files button lets you attach and download supporting documents. Once the order has
                moved past PENDING, a "Signed PDF" button downloads the signed document.
              </p>
            </>
          )}

          {role === "EXPENDITURE_UNIT_HEAD" && (
            <>
              <h6 className="mt-4">Working as an expenditure unit head</h6>
              <p>
                Your dashboard lists requests charged to your expenditure unit, newest first, ten
                per page. Use Details to examine a request, and Files to review its supporting
                documents. Press "Sign and approve" to authorise a request or Reject to deny it —
                approving adds your personal signature on top of the requester's.
              </p>
              <p>
                The <strong>Statistics</strong> button shows accumulated spending grouped by
                supplier, expenditure unit, or period — click any row to expand it and see the
                individual orders or items behind that total.
              </p>
              <p>
                The <strong>Search</strong> button lets you find a product across all your orders:
                start typing a product name and matching results appear automatically.
              </p>
            </>
          )}

          {role === "MANAGEMENT" && (
            <>
              <h6 className="mt-4">Working as management staff</h6>
              <p>
                Your dashboard lists all requests across your department's expenditure units,
                newest first, ten per page. In addition to approving or rejecting pending requests,
                you advance approved orders to PROCESSED and then DELIVERED, and you can delete
                orders.
              </p>
              <p>
                The <strong>Statistics</strong> button shows accumulated spending grouped by
                supplier, expenditure unit, or period — click any row to expand it and see the
                individual orders or items behind that total.
              </p>
              <p>
                The <strong>Search</strong> button lets you find a product across all department
                orders: start typing a product name and matching results appear automatically.
              </p>
            </>
          )}

          {role === "ADMIN" && (
            <>
              <h6 className="mt-4">Administrator</h6>
              <p>
                You see all orders in the system and can perform all actions. The audit trail
                (accessible separately) records every relevant action with its author and
                timestamp. Statistics and Search are also available to you, unrestricted.
              </p>
            </>
          )}

          <h6 className="mt-4">Troubleshooting</h6>
          <ul>
            <li>
              <strong>"Incorrect username or password":</strong> check for typos, especially a
              leading or trailing space; passwords are case-sensitive.
            </li>
            <li>
              <strong>A newly registered account cannot sign in immediately:</strong> confirm
              registration completed successfully; if it failed, the error names the specific
              field that was rejected.
            </li>
            <li>
              <strong>The expenditure unit field shows no results while typing:</strong> try
              searching by the unit's code instead of its name; if the unit is genuinely absent, it
              should be requested from an administrator.
            </li>
            <li>
              <strong>"Sign and submit" or "Sign and approve" fails with a signing error:</strong>{" "}
              this indicates a server-side configuration issue, not something you did incorrectly —
              report it to whoever administers the deployment.
            </li>
            <li>
              <strong>A downloaded signed PDF shows no signature panel:</strong> most browsers'
              built-in viewers don't render signatures — use a dedicated reader such as Adobe
              Acrobat Reader.
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default HelpPage;